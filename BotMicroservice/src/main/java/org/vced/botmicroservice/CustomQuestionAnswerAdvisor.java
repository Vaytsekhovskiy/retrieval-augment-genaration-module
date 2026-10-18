package org.vced.botmicroservice;

import io.micrometer.common.lang.NonNullApi;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.vced.botmicroservice.config.BotConfigService;
import org.vced.ragutillib.document.*;
import org.vced.ragutillib.vector.ChunkService;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.vced.ragutillib.vector.AuthorityChunkFilters.buildAccessFilter;

/**
 * CustomQuestionAnswerAdvisor - переписанный QuestionAnswerAdvisor. Не extends, т.к. нужный конструктор
 * QuestionAnswerAdvisor имеет package-private модификатор доступа
 */
@Slf4j
@NonNullApi
public class CustomQuestionAnswerAdvisor implements BaseAdvisor {
    private boolean isPrintImages;
    public String BASE_URL;
    public String token;
    private final ChunkService chunkService;
    private final DocumentService documentService;
    private final ArticleService articleService;
    private final BotConfigService botConfigService;
    private final int order;
    public static final String RETRIEVED_DOCUMENTS = "qa_retrieved_documents";
    public static final String FILTER_EXPRESSION = "qa_filter_expression";
    @Getter
    private Flux<String> retrievedImages = Flux.empty();
    private static PromptTemplate promptTemplate = new PromptTemplate("""
			{query}
			""");
    public void setRagPrompt(String ragPrompt) throws IllegalArgumentException {
        // Condition 'ragPrompt == null' is always 'false' = ИНТЕЛЛИДЖИ ИДЕЯ ПИЗДАБОЛ!!! НЕ ВЕРЬТЕ ЕМУ!!!
        if (ragPrompt == null || ragPrompt.isBlank()) {
            throw new IllegalArgumentException("Rag prompt cannot be null or blank");
        }
        if (!ragPrompt.contains("{query}")) {
            throw new IllegalArgumentException("Rag prompt must contain '{query}' " +
                                               "because '{query}' is a user question template");
        }
        if (ragPrompt.split("\\{query}", -1).length - 1 < 2) {
            log.warn("Current rag prompt doesn't contain re-Reading feature: https://arxiv.org/pdf/2309.06275");
        }
        if (!ragPrompt.contains("{question_answer_context}")) {
            log.warn("rag prompt doesn't contain '{question_answer_context}'. Retrieved chunks " +
                     "will not be taken into account when generating a response");
        }
        promptTemplate = new PromptTemplate(ragPrompt);
    }

    public CustomQuestionAnswerAdvisor(
            ChunkService chunkService,
            DocumentService documentService,
            ArticleService articleService,
            @Value("${microservice.gateway}") String BASE_URL,
            @Value("$youtrack.api.token") String token,
            @Value("${spring.application.print-images}") boolean isPrintImages,
            String ragPrompt,
            BotConfigService botConfigService,
            int order
    ) throws IllegalArgumentException {
        this.chunkService = chunkService;
        this.documentService = documentService;
        this.articleService = articleService;
        this.BASE_URL = BASE_URL;
        this.token = token;
        this.isPrintImages = isPrintImages;
        setRagPrompt(ragPrompt);
        this.botConfigService = botConfigService;
        this.order = order;
    }

    /**
     * Logic to be executed before the rest of the advisor chain is called.
     * По сути это просто переписанный before() из QuestionAnswerAdvisor, но со своими приколами
     * Дополнительно производится поиск картинок и форматирование текста чанков.
     */

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String query;
        if (chatClientRequest.context().containsKey("query")) {
            query = (String) chatClientRequest.context().get("query");
        } else query = chatClientRequest.prompt().getUserMessage().getText();

        Filter.Expression accessFilter = buildAccessFilter(
                getStringList(chatClientRequest.context().get("authorities"))
        );

        // 1. Search for similar documents in the vector store.
        SearchRequest searchRequestToUse;
        if (botConfigService.isIgnoreAuthorities()) {
            searchRequestToUse = SearchRequest.from(botConfigService.searchRequest())
                    .query(query)
                    .build();
        } else {
            searchRequestToUse = SearchRequest.from(botConfigService.searchRequest())
                    .query(query)
                    .filterExpression(combineFilters(doGetFilterExpression(chatClientRequest.context()), accessFilter))
                    .build();
        }

        List<Document> documents = this.chunkService.similaritySearch(searchRequestToUse);
        setRetrievedImages(documents, (String) chatClientRequest.context().getOrDefault("x-user", ""));

        // 2. Create the context from the documents.
        Map<String, Object> context = new HashMap<>(chatClientRequest.context());
        context.put(RETRIEVED_DOCUMENTS, documents);

        String documentsContext = writeDocumentsContext(documents);

        // 3. Augment the user prompt with the document context.
        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        String augmentedUserText = promptTemplate
                .render(Map.of("query", userMessage.getText(), "question_answer_context", documentsContext));

        // 4. Update ChatClientRequest with augmented prompt.
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .context(context)
                .build();
    }

    private static List<String> getStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>(list.size());
            for (Object o : list) {
                if (o != null) result.add(String.valueOf(o));
            }
            return result;
        }
        return List.of();
    }

    private String writeDocumentsContext(List<Document> documents) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            if (document.getMetadata().containsKey("file_name")) {
                result.append("[Источник: ").append(document.getMetadata().get("file_name")).append("]");
                result.append("\n");
            }
            if (document.getMetadata().containsKey("title")) {
                result.append("[Раздел: ").append(document.getMetadata().get("title")).append("]");
                result.append("\n");
            }
            // ^ нет смысла т.к. путь и заголовок уже прописаны в чанке
            // TODO: разобраться насчёт редких случаев, когда в заголовок попадает текст, и всё не помещается в лимит
            // ^ Зачем вообще разбираться насчёт "редких случаев", если здесь и так нормально было
            result.append(document.getText());
            if (i < documents.size() - 1) {
                result.append("---"); // markdown'овский разделитель "горизонтальная черта", только между чанками
            }
        }
        return result.toString().trim();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        ChatResponse.Builder chatResponseBuilder;
        if (chatClientResponse.chatResponse() == null) {
            chatResponseBuilder = ChatResponse.builder();
        } else {
            chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        }
        chatResponseBuilder.metadata(RETRIEVED_DOCUMENTS, chatClientResponse.context().get(RETRIEVED_DOCUMENTS));
        return ChatClientResponse.builder()
                .chatResponse(chatResponseBuilder.build())
                .context(chatClientResponse.context())
                .build();
    }

    private static Filter.Expression combineFilters(@Nullable Filter.Expression existingFilter, Filter.Expression authFilter) {
        if (existingFilter == null) {
            return authFilter;
        }
        // Комбинируем существующий фильтр с фильтром по ролям через AND
        String combinedExpression = "(" + existingFilter + ") AND (" + authFilter + ")";
        return new FilterExpressionTextParser().parse(combinedExpression);
    }

    /**
     * Вызываем для того, чтобы найти подходящие картинки.
     * Картинки ищем, основываясь на метаданных документа (id, page_number).
     * Ищем те картинки, которые принадлежат тому же документу (id) и находятся на примерно той же странице (page_number)
     */
    public void setRetrievedImages(List<Document> chunks, String XUser) {
        if (isPrintImages) {
            List<String> encodedImages = new ArrayList<>();
            try {
                List<String> imagesFromChunks = chunks.stream()
                        .collect(Collectors.groupingBy(chunk -> chunk.getMetadata().get("id")))
                        .entrySet().stream() // Сгруппировали чанки по id документа для связи с картинками
                        .flatMap(entry -> {
                            long docId = ((Number) entry.getKey()).longValue(); // иногда id приходит в виде Long, иногда Integer, я не знаю почему!!!!!!!
                            List<Document> chunksByDocId = entry.getValue();
                            // Получаем все картинки для данного документа
                            BaseDocument doc = documentService.getDocument(docId);
                            if (doc == null) doc = articleService.getDocument(docId);
                            if (doc != null) {
                                boolean isArticle = doc instanceof Article;
                                return doc.getImages().stream()
                                        // Оставляем те, что попадают хотя бы в один чанк
                                        .filter(image -> chunksByDocId.stream().anyMatch(chunk -> isImageInChunk(image, chunk)))
                                        .map(customImage -> getEncodedImageFromConverter(customImage, XUser, isArticle));
                            }
                            else return Stream.empty();
                        }).distinct().toList();
//                log.info("Images to be sent to frontend: {}", imagesFromChunks);
                // т.к. getEncodedImageFromConverter() нестатичен, его не использовать в .map()
                // поэтому for, иначе создавать сервис для кодировки изображений, lol
                encodedImages.addAll(imagesFromChunks);
            } catch (ResourceAccessException e) {
                log.error("Error at setRetrievedImages: {}", e.getMessage());
                encodedImages.add("\"\nОШИБКА ГЕНЕРАЦИИ ИЗОБРАЖЕНИЙ: " +
                        "На сервере не запущен генератор изображений, сообщите администратору и повторите запрос позже\"");
            }
            retrievedImages = Flux.fromIterable(encodedImages);
        }
    }

    /**
     * Вспомогательная функция для проверки, находится ли картинка внутри отрывка.
     */
    public static boolean isImageInChunk(CustomImage image, Document chunk) {
        // не забываем что в pdf координаты идут из левого нижнего угла (т.е. снизу вверх)!
        Map<String, Object> metadata = chunk.getMetadata();
        String docType = (String) metadata.get("doc_type");

        // Если это статья - ВСЕГДА включаем изображения
        if ("article".equals(docType)) {
            return true;
        }


        // Проверка на null из-за очень редких крайних случаев, когда в чанке нет нужных метаданных
        Object positionObj = metadata.get("position");
        Object endPositionObj = metadata.get("end_position");
        Object pageNumberObj = metadata.get("page_number");
        Object endPageNumberObj = metadata.get("end_page_number");

        if (positionObj == null || endPositionObj == null || pageNumberObj == null || endPageNumberObj == null) {
            return false;
        }

        try {
            float startPos = ((Number) positionObj).floatValue();
            float endPos = ((Number) endPositionObj).floatValue();
            int startPage = ((Number) pageNumberObj).intValue();
            int endPage = ((Number) endPageNumberObj).intValue();


            // Если чанк занимает одну страницу
            if (startPage == endPage) {
                return image.getPage() == startPage && image.getYPos() <= startPos && image.getYPos() >= endPos;
            }
            // Если картинка на первой странице чанка
            if (image.getPage() == startPage) {
                return image.getYPos() <= startPos;
            }
            // Если картинка на последней странице чанка
            if (image.getPage() == endPage) {
                return image.getYPos() >= endPos;
            }
            // Если картинка между начальной и конечной страницами чанка
            if (image.getPage() > startPage && image.getPage() < endPage) {
                return true;
            }
        } catch (ClassCastException | NullPointerException e) {
            log.warn("Error checking image in chunk: {}", e.getMessage());
        }
        return false;
    }

    @SneakyThrows
    private String getEncodedImageFromConverter(CustomImage image, String XUser, boolean isArticle)
            throws ResourceAccessException {

        RestClient restClient = RestClient.create();
        ResponseEntity<String> response;

        try {
            response = restClient.get()
                    .uri(BASE_URL + "/api/convert/image/" + image.getId()
                            + "?x-user=" + XUser
                            + "&is-article=" + isArticle)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toEntity(String.class);
        } catch (HttpClientErrorException e) {
            log.error("ConvertMicroservice currently down: {}", e.getMessage());
            response = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return Objects.requireNonNull(response.getBody());
    }


    /*
        Из документации (https://docs.spring.io/spring-ai/reference/api/advisors.html#_advisor_order):
        The execution order of advisors in the chain is determined by the getOrder() method. Key points to understand:
            Advisors with lower order values are executed first.
            ...
     */
    @Override
    public int getOrder() {
        return order;
    }

    @Nullable
    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        if (!context.containsKey(FILTER_EXPRESSION)
                || !StringUtils.hasText(context.get(FILTER_EXPRESSION).toString())) {
            return botConfigService.searchRequest().getFilterExpression();
        }
        return new FilterExpressionTextParser().parse(context.get(FILTER_EXPRESSION).toString());
    }
}
