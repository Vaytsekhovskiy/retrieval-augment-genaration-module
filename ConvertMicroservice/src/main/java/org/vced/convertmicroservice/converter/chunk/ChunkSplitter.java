package org.vced.convertmicroservice.converter.chunk;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.id.RandomIdGenerator;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.vced.convertmicroservice.converter.file.DocumentContentExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@PropertySource("classpath:application.yml")
@Component
public class ChunkSplitter extends TokenTextSplitter {
    /*
     * Вот здесь начинается самое страшное, потому что разрабы спринга полнейшие идиоты,
     * которые позакрывали важные данные от разработчиков, сделав некоторые поля и методы private.
     * Поэтому здесь и появился этот класс.
     * Существует некоторый класс JTokkitTokenCountEstimator, который подсчитывает кол-во токенов в чанке и сравнивает
     * их с максимально возможным количеством токенов для одного чанка, который можно впихнуть в документ.
     * Хоть там и существует строка: private static final int MAX_INPUT_TOKEN_COUNT = 8191;
     * Этот класс (JTokkitTokenCountEstimator) используется где-то глубоко внутри VectorStore, поэтому для каждой
     * векторной БД свои максимальные размеры :) Немного поломав голову и подебажив код, я обнаружил,
     * что максимальное количество токенов для одного чанка для chroma db ( и pgvector) = 7372, что я и написал в поле моего
     * класса ниже (UPD: уже не актуально).
     * В ИДЕАЛЕ КОНЕЧНО НУЖНО ЛЕЗТЬ В "КИШКИ" СПРИНГА ПРИ ПОМОЩИ Reflection ИЛИ BeanPostProcessor, ДЛЯ ТОГО, ЧТОБЫ
     * ДОСТАТЬ ЗНАЧЕНИЕ MAX_INPUT_TOKEN_COUNT ИЗ БИНА JTokkitTokenCountEstimator, КОТОРЫЙ ГДЕ-ТО НАХОДИТСЯ В
     * КОНТЕКСТЕ СПРИНГА, НО ПОКА БЫЛО РЕШЕНО ИСПОЛЬЗОВАТЬ БРУТ ФОРС И НАДЕЯТСЯ НА ТО, ЧТО НИЧЕГО НЕ ПОМЕНЯЕТСЯ
     * ПРИ СЛЕДУЮЩЕМ ЗАПУСКЕ. ОЧЕВИДНО, ЭТО НАДО БУДЕТ ИСПРАВИТЬ! ОТПИШУСЬ, КАК ВСЁ СДЕЛАЮ!
     * int MAX_INPUT_TOKEN_COUNT = 7372; // Максимальный размер чанка для хромы (возможно)
     *
     * UPD (16.06.2025) На данный момент выбираем int MAX_INPUT_TOKEN_COUNT = 512 из-за ограничений ембеддинг модели
     */
    @Value("${spring.ai.embedding.chunk.pretty-metadata}")
    @Getter
    private boolean isPrettyMetadata; // оформление метаданных в красивый "хардкодный" формат или автоматом по ключу
    @Value("${spring.ai.embedding.chunk.max-size}")
    @Getter
    private int MAX_INPUT_TOKEN_COUNT;
    public static final JTokkitTokenCountEstimator tokenCountEstimator = new JTokkitTokenCountEstimator();

    public List<List<Document>>
    splitParagraphs(List<Document> chunkedFileText) {
        List<List<Document>> splitChunkedFileText = new ArrayList<>();
        int tokenCount;
        for (Document document : chunkedFileText) {
            String text = document.getText();
            if (text == null) continue;
            tokenCount = tokenCountEstimator.estimate(text);
            if (tokenCount > MAX_INPUT_TOKEN_COUNT) {
                /*
                    Сколько я бы не выдумывал и не писал новых реализаций для логики разделения
                    больших параграфов на более малые чанки, АБСОЛЮБНО СУКА ВСЁ работает крайне парашно
                    с жуткими ограничениями. Приведу список того, что я пытался делать
                    1. В тупую посимвольно разбивать параграф. Работает 100%, но работает хуёво, т.к.
                        не учитывается смысл чанка + проблема с md таблицами + можно разбить так, чтобы одна часть
                        слова находилась в одном чанке, а другая в другом
                    2. Использование встроенного в Spring AI TokenTextSplitter. Не работает от слова совсем. Да,
                        он пытается разделить по пробелам, точкам, запятым и всё такое, но делает очень плохо
                        (как минимум тупо может обрезать полезную нам часть и "потерять её") + не учитывает md.
                        Для того чтобы узнать следующие проёбы TokenTextSplitter, см.
                    3. Оставить всё как есть. К сожалению не работает. Т.к. будет работать следующим образом
                        if (tokenCountEstimator.estimate(document.getText()) > MAX_INPUT_TOKEN_COUNT)
                            Обрезаем всё, что идёт после MAX_INPUT_TOKEN_COUNT
                    4. Способ, который я буду реализовывать сейчас. Свой кастомный TokenTextSplitter, который
                        будет учитывать всё, что мне нужно
                    5. TODO: в идеале, конечно, использовать семантическое деление на чанки, но как это сделать
                        правильно, я пока не знаю
                 */
                List<Document> splitParagraphDocuments = doEnterSplit(text, MAX_INPUT_TOKEN_COUNT).stream().map(
                        string -> document.mutate().text(string).id(new RandomIdGenerator().generateId()).build()
                ).toList();
                splitChunkedFileText.add(splitParagraphDocuments);
            } else splitChunkedFileText.add(List.of(document));
        }
        splitChunkedFileText.forEach(ChunkSplitter::approximateChunkPosition);
        return splitChunkedFileText;
    }

    /**
     * Примерно оценивает и перезаписывает номера страниц и координаты чанков, порождённых одним параграфом (главой),
     * предполагая что чанки распределены по страницам равномерно
     *
     * @param chunkedParagraph Список невекторизированных чанков, все от одного параграфа
     * @return Тот же список, но с обновлёнными метаданными чанков
     */
    public static List<Document> approximateChunkPosition(List<Document> chunkedParagraph) {
//        Очень спорная темка на самом деле предполагать, что все чанки одной длины, с вероятностью 99% это не так, я лишь надеюсь, что это прокатит. На будущее можно сделать так
//        paragraphSize - сюда будем записывать размер параграфа в токенах
//        paragraphSIze = tokenCountEstimator.estimate(
//                chunkedParagraph.stream()
//                        .map(Document::getText)
//                        .collect(Collectors.joining())
//        );
//        Аналогично с chunkSIze:
//        chunkSize = tokenCountEstimator.estimate(
//                chunk.getText()
//        );
//        И уже как то плясать от этого, такой метод будет очевидно лучше, но думаю, текущая реализация может сойти
//        TODO: аппроксимировать размер чанков по их длине в токенах а не равномерно по страницам
        var paragraphMetadata = chunkedParagraph.getFirst().getMetadata();
        Integer startPage = (Integer) paragraphMetadata.get("page_number");
        Integer endPage = (Integer) paragraphMetadata.get("end_page_number");
        Float startPos = (Float) paragraphMetadata.get("position");
        Float endPos = (Float) paragraphMetadata.get("end_position");

        if (startPos != null && endPos != null && startPage != null && endPage != null) {
            // коорды в пдфках идут снизу вверх, поэтому 1 - position; представь документ как числовую прямую
            float startAbsolutePos = startPage + (1 - startPos);
            float endAbsolutePos = endPage + (1 - endPos);
            float paragraphSize = endAbsolutePos - startAbsolutePos;
            int chunkCount = chunkedParagraph.size();
            float chunkSize = paragraphSize / chunkCount;
            for (int i = 0; i < chunkCount; i++) {
                var chunkMetadata = chunkedParagraph.get(i).getMetadata();
                chunkMetadata.put("position", 1 - ((startAbsolutePos + i * chunkSize) % 1));
                chunkMetadata.put("page_number", (int) (startAbsolutePos + i * chunkSize));
                chunkMetadata.put("end_position", 1 - ((startAbsolutePos + (i + 1) * chunkSize) % 1));
                chunkMetadata.put("end_page_number", (int) (startAbsolutePos + (i + 1) * chunkSize));
            }
        }
        return chunkedParagraph;
    }

    public List<Document> addTables(List<Document> documents, List<String> tablesText) {
        // итоговый лист, в котором находятся md таблицы + размер всех доков не превышает MAX_INPUT_TOKEN_COUNT
        List<Document> documentsWithTables = new ArrayList<>();
        for (Document document : documents) {
            List<Document> updDocuments = List.of(document);
            if (document.getText() != null &&
                    DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(document.getText()).find()) {
                String updDocText = document.getText();
                for (int i = 0; i < tablesText.size(); i++) {
                    if (updDocText.contains(DocumentContentExtractor.getIdxTableMarker(i))) {
                        updDocText =
                                DocumentContentExtractor.replaceTableMarkerToTable(
                                        updDocText,
                                        tablesText.get(i),
                                        i
                                );
                        // Не забываем привести новые таблицы в красивый вид
                        updDocText = DocumentContentExtractor.fixStringTable(updDocText);
                    }
                    if (!DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(updDocText).find())
                        break;
                }
                List<String> updDocTextSplit = doEnterSplit(updDocText, MAX_INPUT_TOKEN_COUNT);
                // Пересоздаём документ заново, потому что разрабы спринга твари

                updDocuments = updDocTextSplit.stream()
                        .map(text -> document.mutate()
                                .text(text)
                                .id(new RandomIdGenerator().generateId())
                                .build())
                        .toList();
            }
            documentsWithTables.addAll(updDocuments);
        }
        return combineSmallChunks(documentsWithTables, MAX_INPUT_TOKEN_COUNT);
    }

    /**
     * Добавление метаданных в контент чанка с целью улучшить поиск конкретного чанка по ключевым словам внутри контента
     *
     * @param documents - список чанков, в контент которых необходимо добавить метаданные
     * @param metadata  - словарь с метаданными
     * @param addKey    - добавлять ли ключ в контент метаданных или нет
     *                  (С одной стороны больше контекста для нейросети,
     *                  с другой стороны для всех чанков будет набор похожих слов, что делает чанки "ближе")
     * @return Список чанков, в которые были добавлены метаданные в начало контента
     */
    public List<Document> addMetadata(List<Document> documents, Map<String, String> metadata, boolean addKey) {
        if (metadata == null || metadata.isEmpty() || documents == null || documents.isEmpty()) return documents;
        StringBuilder metadataContent = new StringBuilder(); // то, что мы вставим в каждый чанк
        // Инициализируем metadataContent
        if (isPrettyMetadata && (metadata.containsKey("document_path") || metadata.containsKey("title"))) {
            // [ИСТОЧНИК: /Доки/Пример.pdf | Раздел: Заголовок главы]
            metadataContent.append("[ИСТОЧНИК: ");
            if (metadata.containsKey("document_path")) {
                metadataContent.append(metadata.get("document_path"));
            }
            if (metadata.containsKey("title")) {
                if (metadata.containsKey("document_path")) {
                    metadataContent.append(" | ");
                }
                metadataContent.append("Раздел: ").append(metadata.get("title"));
            }
            metadataContent.append("]\n");
        } else {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (addKey) {
                    metadataContent.append(key).append(" : ").append(value).append("\n");
                } else {
                    metadataContent.append(value).append("\n");
                }
            }
        }
        int metadataSize = tokenCountEstimator.estimate(metadataContent.toString());
        if (metadataSize > MAX_INPUT_TOKEN_COUNT) {
            log.warn("""
                    Metadata content:\s
                     {}\s
                     is longer than maximum chunk size, skip adding metadata to chunks content""", metadataContent);
            return documents;
        }
        List<Document> splitChunks = new ArrayList<>();
        int docSizeWithoutMetadata = MAX_INPUT_TOKEN_COUNT - metadataSize;
        // Разобью чанки на длину не больше чем docSizeWithoutMetadata, чтобы поместились метаданные
        for (Document document : documents) {
            if (tokenCountEstimator.estimate(document.getText()) > MAX_INPUT_TOKEN_COUNT) {
                log.error("chunk: {} is longer than maximum chunk size, skip adding metadata to chunks content",
                        document.getId());
                return documents;
            } else if (tokenCountEstimator.estimate(document.getText()) + metadataSize > MAX_INPUT_TOKEN_COUNT) {
                var splitChunk = doEnterSplit(document.getText(), docSizeWithoutMetadata)
                        .stream()
                        .map(splitContent -> document
                                .mutate()
                                .text(splitContent)
                                .id(new RandomIdGenerator().generateId())
                                .build()
                        )
                        .toList();
                splitChunks.addAll(splitChunk);
            } else {
                splitChunks.add(document);
            }
        }
        // Объединю мелкие чанки с учётом docSizeWithoutMetadata
        var combinedChunks = combineSmallChunks(splitChunks, docSizeWithoutMetadata);
        ArrayList<Document> chunksWithMetadata = new ArrayList<>();
        // Для каждого чанка добавлю метаданные
        for (Document document : combinedChunks) {
            chunksWithMetadata.add(document.mutate().text(metadataContent + document.getText()).build());
        }
        return chunksWithMetadata;
    }
    /**
     * Разбиение больших чанков по "\n", является надстройкой над оригинальным методом tokenTextSplitter.doSplit(),
     * который не учитывает "приоритет" разбиения чанков по "\n"
     *
     * @param text - текст, который нужно разбить
     * @param textTokenLimit  - максимальный размер текста
     * @return список текстов, по которым был разбит оригинальный text
     */
    private List<String> doEnterSplit(String text, int textTokenLimit) {
        if (textTokenLimit > MAX_INPUT_TOKEN_COUNT) {
            log.warn("textTokenLimit insdie doEnterSplit method is longer than maximum chunk size, " +
                    "use MAX_INPUT_TOKEN_COUNT instead");
            textTokenLimit = MAX_INPUT_TOKEN_COUNT;
        }
        String[] rows = text.split("\n");
        List<String> splitText = new ArrayList<>();

        StringBuilder curChunk = new StringBuilder();

        for (String row : rows) {
            String trimmedRow = row.trim();

            // Если строка сама по себе слишком большая — дробим её отдельно
            if (tokenCountEstimator.estimate(trimmedRow) > textTokenLimit) {
                if (!curChunk.isEmpty()) {
                    splitText.add(curChunk.toString().trim());
                    curChunk = new StringBuilder();
                }
                List<String> split = doSplit(trimmedRow, textTokenLimit);
                for (String part : split) {
                    splitText.add(part.trim());
                }
                continue;
            }

            // Пробуем записать новую строку, увеличивая текущий чанк до лимита
            String candidate = !curChunk.isEmpty()
                    ? curChunk + "\n" + trimmedRow
                    : trimmedRow;

            // Если чанк слишком большой, используем предыдущий вариант
            if (tokenCountEstimator.estimate(candidate) > textTokenLimit) {
                if (!curChunk.isEmpty()) {
                    splitText.add(curChunk.toString().trim());
                }
                curChunk = new StringBuilder(trimmedRow);
            // иначе записываем получившийся чанк
            } else {
                if (!curChunk.isEmpty()) {
                    curChunk.append("\n");
                }
                curChunk.append(trimmedRow);
            }
        }

        if (!curChunk.isEmpty()) {
            splitText.add(curChunk.toString().trim());
        }

        return splitText;
    }

    /**
     * Соединение маленьких чанков между собой. Если в списке есть два малых чанка,
     * сумма текста которых меньше textTokenLimit, объединяем эти два чанка в один
     *
     * @param documents - список чанков, которые нужно соединить
     * @param textTokenLimit  - максимальный размер текста
     * @return список объединённых чанков
     */
    public List<Document> combineSmallChunks(List<Document> documents, int textTokenLimit) {
        if (textTokenLimit > MAX_INPUT_TOKEN_COUNT) {
            log.warn("textTokenLimit insdie combineSmallChunks method is longer than maximum chunk size, " +
                    "use MAX_INPUT_TOKEN_COUNT instead");
            textTokenLimit = MAX_INPUT_TOKEN_COUNT;
        }
        List<Document> resultList = new ArrayList<>();
        if (documents.isEmpty()) {
            return resultList;
        }

        Document combined = documents.getFirst();

        for (int i = 1; i < documents.size(); i++) {
            Document next = documents.get(i);

            String mergedText = combined.getText() + next.getText();
            if (tokenCountEstimator.estimate(mergedText) < textTokenLimit) {
                combined = combined.mutate()
                        .text(mergedText)
                        .id(new RandomIdGenerator().generateId())
                        .build();
            } else {
                resultList.add(combined);
                combined = next;
            }
        }
        resultList.add(combined); // don't forget last one
        return resultList;
    }


}