package org.vced.convertmicroservice.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.JodConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.vced.convertmicroservice.converter.access.AccessReader;
import org.vced.convertmicroservice.converter.chunk.ChunkMetadataEnricher;
import org.vced.convertmicroservice.converter.chunk.ChunkSplitter;
import org.vced.convertmicroservice.converter.file.*;
import org.vced.convertmicroservice.watcher.FilePathAbstractService;
import org.vced.ragutillib.document.*;
import org.vced.ragutillib.vector.ChunkService;
import org.vced.ragutillib.vector.VectorConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author Егор
 * Класс для конвертации различных файлов
 */
@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
@Import(value = {DocumentConfig.class, VectorConfig.class})
@Slf4j
public class ConverterService {
    private final ChunkMetadataEnricher chunkMetadataEnricher;
    @Value("${main.file.pdf-root}")
    private String pdfRoot;
    private final FilePathAbstractService filePathAbstractService;
    @Value("${main.make-chunk-summary}")
    private boolean makeChunkSummary;
    private final StrangeDocumentHandler strangeDocumentHandler;
    private final DocumentService documentService;
    private final ChunkService chunkService;
    private final ObjectMapper objectMapper;
    private final ChunkSplitter chunkSplitter;
    private final ContentExtractorProvider contentExtractorProvider;
    private final AccessReader accessReader;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(); // строгая очередь

    @PostConstruct
    public void initConverterService() {

        // Zip bomb detected — POI считает,
        // что один из файлов внутри DOCX слишком сильно сжат,
        // и расценивает это как потенциальную атаку.
        // Здесь мы убавили минимальное сжатие, чтобы POI не считал наши доки как зип бомбу
        ZipSecureFile.setMinInflateRatio(0.0005); // или 0.0001
        ZipSecureFile.setMaxEntrySize(500 * 1024 * 1024); // 500 Mb на всякий случай
        ZipSecureFile.setMaxTextSize(500 * 1024 * 1024);
    }

    public CompletableFuture<Void> receiveDocument(String sentFile) {
        return CompletableFuture.runAsync(
                () -> receiveDocumentBlocking(sentFile),
                executor
        );
    }

    /**
     * @param sentFile принимает путь документа и его состояние (добавлен, изменён, удалён).
     *                 С начала проверяем, существует ли похожий документ с одинаковым документом.
     *                 Потом берем путь pdf без корневого, и имя без расширения.
     *                 Создаем новый файл, который имеет расширение pdf.
     *                 Пытается создать папки для него с учетом названия
     *                 Далее, используя библиотеку JodConverter переводоим файлы полученный файл в нужную папку с форматом pdf
     *                 Обычный порт это 2002
     *                 После перевода останаливаем менеджера его работу
     */
    @KafkaListener(topics = "filesCheck", autoStartup = "${auto-startup:true}")
    public void receiveDocumentBlocking(@Payload String sentFile) {
        Map.Entry<String, FilePathAbstractService.FILE_STATE> sentFilePath_State;
        try {
            sentFilePath_State =
                    objectMapper.readValue(sentFile, new TypeReference<>() {
                    });
        } catch (JsonProcessingException e) {
            log.error("Incorrect kafka data topic received. Only accept path + FILE_STATE json");
            return;
        }
        // блок try на весь метод нужен чтобы receive не крашил весь сервис
        try {
            String filePath = sentFilePath_State.getKey();
            FilePathAbstractService.removeConversionState(filePath);
            Path path = Path.of(filePath);
            if (filePath.isBlank()
                    || filePath.contains("Thumbs.db")
                    || filePath.contains("~$")
                    || filePath.contains(".~lock")
                    || filePath.endsWith(".lnk")
                    || !Files.exists(path)
                    || !Files.isRegularFile(path)
                    || !Files.isReadable(path)
                    || Files.size(path) == 0
            )
                return; // мета, временные, несуществующие, нечитаемые, нулевые файлы + папки
            FilePathAbstractService.FILE_STATE fileState = sentFilePath_State.getValue();
            if (fileState != FilePathAbstractService.FILE_STATE.CREATED) {
                CustomDocument foundedDoc = documentService.getDocumentByOriginalPath(filePath);
                if (foundedDoc != null) {
                    documentService.deleteDocument(foundedDoc);
                    chunkService.deleteChunksByMetadataId(foundedDoc.getId());
                    log.warn("Document {} was removed", filePath);
                } else log.info("Deleted document {} didn't exist in database", filePath);
                if (fileState == FilePathAbstractService.FILE_STATE.DELETED) {
                    // обновляем директорию, учитывая удалённый док
                    if (foundedDoc != null)
                        strangeDocumentHandler.handeStrangeDocuments(Path.of(foundedDoc.getOriginalPath()).getParent());
                    return;
                }
                // MODIFIED ...
            }
            // Проверка на существующий док
            if (documentService.getDocumentByOriginalPath(filePath) != null) {
                log.error("document {} will be ignored, this doc already in db ",
                        filePath);
                return;
            }
            convertDocument(filePath);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        } finally {
            FilePathAbstractService.removeConversionState(sentFilePath_State.getKey());
        }
    }

    private String preprocessText(String text) {
        // Убираем переносы слова
        text = text.replaceAll("- *[\n\r]+ *", "");
        // Убираем багулю с переносом, когда такового нет
        text = fixLineBreaks(text);
        // Убираем всякие повторяющиеся пробелы, "-", итд. Спорный подход
        // С одной стороны меньше токенов для модели
        // с другой, часть важной информации убирается??
        // TODO: придумать другой более логичный способ
        text = text.replaceAll(" {2,}", " ");
        text = text.replaceAll("_{4,}", " ");
        return text;
    }

    public static String fixLineBreaks(String text) {
        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("[\r\n]+");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (i < lines.length - 1) {
                String nextLine = lines[i + 1].trim();
                if (!line.endsWith(".") && !line.endsWith(":") && !line.endsWith("?") &&
                        !line.endsWith("!") && !line.endsWith(";") &&
                        !nextLine.isEmpty() && Character.isLowerCase(nextLine.charAt(0))) {
                    sb.append(line).append(" "); // склеиваем с пробелом
                    continue;
                }
            }
            sb.append(line).append("\n");
        }

        return sb.toString();
    }


    @Value("${main.file.convert-to-text}")
    private boolean convertToText;

    /**
     * @param documentPath - оригинальный документ
     */
    private void convertDocument(String documentPath) {
        FilePathAbstractService.putConversionState(documentPath, FilePathAbstractService.CONVERSION_STATE.PROCESSING);
        // Выбирай нужный нам путь
        String rootPath = filePathAbstractService.getRootPaths()
                .stream()
                .filter(documentPath::contains)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Root path not found for " + documentPath));
        Path documentPathWithoutRoot = Path.of(documentPath.replace(rootPath, ""));
        String pdfPathWithoutRoot = documentPathWithoutRoot.getParent().toString();
        String fileNameWithoutExtension = FilenameUtils.getBaseName(documentPath);
        File outputPdfFile = new File(pdfRoot + pdfPathWithoutRoot + "/" + fileNameWithoutExtension + ".pdf");
        outputPdfFile.getParentFile().mkdirs(); // trying to create directories
        try {
            if (!documentPath.endsWith(".pdf")) {
                JodConverter
                        .convert(new File(documentPath))
                        .to(outputPdfFile)
                        .execute();
            } else {
                if (Files.exists(outputPdfFile.toPath())) {
                    Files.delete(outputPdfFile.toPath());
                }
                Files.copy(Path.of(documentPath), outputPdfFile.toPath());
            }
        } catch (OfficeException e) {
            log.error("Failed to convert pdf file {}", documentPath, e);
        } catch (IOException e) {
            log.error("Failed to copy pdf file {} to {}", documentPath, outputPdfFile, e);
        }

        String outputPdfFilePath = outputPdfFile.getAbsolutePath();
        DocumentType documentType;
        // Определяем тип документа
        try {
            documentType = DocumentType.valueOf(FilenameUtils.getExtension(documentPath).toUpperCase());
        } catch (IllegalArgumentException e) {
            if (DocxContentExtractor.isXWPFDocument(documentPath)) documentType = DocumentType.DOCX;
            else documentType = DocumentType.PDF;
        }
        DocumentContentExtractor extractor = contentExtractorProvider.getDocumentTableExtractor(documentType);
        // получаем таблицы из дока
        Map.Entry<File, List<String>> pdfWithoutTables_tablesText =
                extractor.extractTables(documentPath, outputPdfFilePath);
        List<String> tablesText = pdfWithoutTables_tablesText.getValue();
        // получаем картинки из документа
        List<CustomImage> imagesList = extractor.extractImages(outputPdfFilePath, pdfPathWithoutRoot);


        String textFromFile = "";
        if (convertToText) {
            textFromFile = DocumentContentExtractor.replaceTableMarkersToTables(
                    preprocessText(
                            DocumentContentExtractor.extractText(
                                    pdfWithoutTables_tablesText.getKey()
                            )
                    ),
                    tablesText
            );
            textFromFile = DocumentContentExtractor.fixStringTable(textFromFile);
        }
        // получаем права доступа на документ
        List<String> authorities = accessReader.getAcl(documentPath);

        // Создаём customDocument, который будет храниться в бд
        CustomDocument customDocument = CustomDocument
                .builder()
                .name(outputPdfFile.getName())
                .path(outputPdfFilePath)
                .originalPath(documentPath)
                .images(imagesList)
                .content(Collections.singleton(textFromFile))
                .authorities(authorities)
                .build();

        // Начинаем делить документ на чанки

        try {
            List<Document> chunkedFileText;
            try {
                /*
                    Проверка на наличие Table Of Contents (TOC) внутри документа,
                    если TOC существует, разделяем чанки с его учётом, иначе разбиваем текст
                    основываясь исключительно на разделителях (пробелы, новые строки, знаки пунктуации...)
                 */
                CustomParagraphPdfDocumentReader pdfReader = new CustomParagraphPdfDocumentReader(
                        "file:" + pdfWithoutTables_tablesText.getKey().getAbsolutePath(),
                        PdfDocumentReaderConfig.builder()
                                .withReversedParagraphPosition(true) // спасибо разрабам за непонятные доки
                                .build());
                /*
                    Здесь pdfReader добавляет следующие метаданные:
                    - page_number
                    - level
                    - file_name (именно PDF файла)
                    - end_page_number
                    - position
                    - title
                    - end_position
                 */
                chunkedFileText = pdfReader.get();
                chunkedFileText.forEach(chunk -> chunk.getMetadata().put("is_strange", false));
            } catch (Throwable e) {
                // IndexOutOfBoundsException
                // IllegalArgumentException
                // Document outline (e.g. TOC) is null. Срабатывает в случае, если у документа нет TOC...
                chunkedFileText = List.of(Document.builder()
                        .text(textFromFile)
                        .metadata(Map.of("file_name", customDocument.getName(),
                                "page_number", 1,
                                "end_page_number", PdfContentExtractor.extractPageNumber(outputPdfFilePath),
                                "position", (float) 1.0,
                                "end_position", (float) 0.0,
                                "is_strange", true))
                        .build());
                customDocument.setStrange(true);
            }
            CustomDocument customDocumentFromDB = documentService.saveDocument(customDocument);
            // Предобрабатываем текст и фильтруем мелкие чанки
            List<Document> chunkedFileProcessedText = chunkedFileText.stream()
                    .map(document1 ->
                            new Document(
                                    preprocessText(document1.getText()), // Обработка текста в более красивый вид
                                    document1.getMetadata()))
                    .toList().stream().filter(document1 ->
                            document1.getText() != null &&
                                    (ChunkSplitter.tokenCountEstimator.estimate(document1.getText()) > 70 ||
                                            DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(document1.getText()).find()))
                    .toList();
            // Добавляем дополнительные метаданные для того, чтобы SearchService нас "понимал"
            chunkedFileProcessedText.forEach(document1 -> {
                document1.getMetadata().put("id", customDocumentFromDB.getId());
                document1.getMetadata().put("authorities", authorities);
                document1.getMetadata().put("doc_type", "custom");
            });
            // Дополнительно делим большие параграфы на чанки. В каждом внутреннем списке находится разбитый параграф
            List<List<Document>> chunkedFilePrecessedTextWithSplitParagraphs =
                    chunkSplitter.splitParagraphs(chunkedFileProcessedText);

            // В каждом чанке меняем CustomTableExtractor.TABLE_REMOVED_MARKER на красивую таблицу
            chunkedFilePrecessedTextWithSplitParagraphs = chunkedFilePrecessedTextWithSplitParagraphs.stream().map(
                    documents -> chunkSplitter.addTables(documents, tablesText)
            ).toList();
            // Для каждого чанка добавляем метаданные в контент документа для улучшения качества поиска
            chunkedFilePrecessedTextWithSplitParagraphs = chunkedFilePrecessedTextWithSplitParagraphs
                    .stream()
                    .map(titleChunks -> {
                        Map<String, String> orderedMetadata = new LinkedHashMap<>();
                        orderedMetadata.put("document_path", documentPathWithoutRoot.toString());
                        var titleChunk = titleChunks.getFirst();
                        if (titleChunk != null && titleChunk.getMetadata().get("title") != null)
                            orderedMetadata.put("title", titleChunk.getMetadata().get("title").toString());
                        return chunkSplitter.addMetadata(titleChunks, orderedMetadata, true);
                    })
                    .toList();
            /*
                Для каждого чанка делаем вывод при помощи модели ИИ, если стоит makeChunkSummary
            */
            try {
                log.debug("Saving chunks for the document: {} to the database...\n chunks count: {}",
                        documentPath, chunkedFilePrecessedTextWithSplitParagraphs.stream()
                                .mapToInt(List::size)
                                .sum());
                long startAll = System.currentTimeMillis();
                List<Document> chunksFromDb;
                if (makeChunkSummary) {
                    List<Document> chunkedFilePrecessedTextWithSplitParagraphsSummary =
                            chunkedFilePrecessedTextWithSplitParagraphs.stream()
                                    .map(chunkMetadataEnricher::enrichDocuments)
                                    .flatMap(List::stream)
                                    .toList();
                    chunksFromDb = chunkService.saveChunks(chunkedFilePrecessedTextWithSplitParagraphsSummary);

                } else chunksFromDb = chunkService.saveChunks(chunkedFilePrecessedTextWithSplitParagraphs.stream()
                        .flatMap(List::stream)
                        .toList());
                long tookAll = System.currentTimeMillis() - startAll;
                log.debug("Chunks for the document: {} saved successfully to database...\n chunks count: {}\n took: {} ms",
                        documentPath, chunksFromDb.size(), tookAll);
            } catch (Throwable e) {
                log.error("Document {} took to long to process. Read timed out (30m), not enough system sources" +
                                "OR Ollama not working",
                        documentPath, e);
                chunkService.saveChunks(chunkedFilePrecessedTextWithSplitParagraphs.stream()
                        .flatMap(List::stream)
                        .toList());
            }

        } catch (RuntimeException | IllegalAccessError throwable) {
            /*
             * RuntimeEcxeption - Index -1 out of bounds
             * NullPointerException - "document1" is null
             * IllegalAccessError - Document outline (e.g. TOC) is null. ИЛИ maximum number of allowed input tokens
             * Все эти ошибки указывают на то, что данный документ максимально "странный",
             * С вероятностью 99% это шаблоны и прочие бесполезные в плане информации формы
             * С такими документами нужно иметь дело отдельно
             * Подобные документы НЕ заносятся в векторную БД
             */
            log.warn("found strange document: \n {} \n document will be marked as \"strange\"\n{}", outputPdfFilePath, throwable.getMessage());
            if (throwable.getMessage().contains("Tokens in a single document exceeds the maximum number of allowed input tokens")) {
                // вывожу эту ошибку на всякий случай, это будет значить, что CustomChunkSplitter() плохо поделил
                // параграф на чанки
                log.error(throwable.getMessage());
            }
            customDocument.setStrange(true);
            documentService.saveDocument(customDocument);
        } finally {
            strangeDocumentHandler.handeStrangeDocuments(Path.of(customDocument.getOriginalPath()).getParent());
        }
    }
}
