package org.vced.convertmicroservice.converter.chunk;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.id.RandomIdGenerator;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.vced.convertmicroservice.converter.file.DocumentContentExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// БОЛЕЕ ЛУЧШИЙ АЛГОРИТМ ПО СРАВНЕНИЮ С ChunkSplitter, но с багами, как решить эти баги,
// я не додумался TODO: взять этот алгоритм
@Deprecated(forRemoval = true)
public class SmartChunkSplitter extends TokenTextSplitter {
    private int MAX_INPUT_TOKEN_COUNT;
    public static final JTokkitTokenCountEstimator tokenCountEstimator = new JTokkitTokenCountEstimator();
    public List<Document> addTables(List<Document> documents, List<String> tablesText) {
        // итоговый лист, в котором находятся md таблицы + размер всех доков не превышает MAX_INPUT_TOKEN_COUNT
        List<Document> documentsWithTables = new ArrayList<>(documents.size());
        List<Document> tmp = new ArrayList<>(documents);
        while (
                DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(
                        tmp.stream().map(Document::getText).collect(Collectors.joining())
                ).find()
        ){
            int remainderTokenCount = 0;
            String remainder = "";
            String remainderTable = "";
            for (int j = 0; j < tmp.size(); j++) {
                Document document = tmp.get(j);
                Document updDocument = document;
                if (!remainder.isBlank()) {
                    documentsWithTables.addAll(
                            j,
                            handleRemainder(updDocument, remainder, remainderTokenCount, remainderTable)
                    );
                    remainder = "";
                }
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
                            List<String> data = processTextWithBigMdTable(updDocText, tablesText.get(i));
                            remainder = data.getLast();
                            remainderTable = tablesText.get(i);
                            remainderTokenCount = tokenCountEstimator.estimate(remainder);
                            updDocText = data.getFirst();
                        }
                        if (!DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(updDocText).find())
                            break;
                    }
                    // Пересоздаём документ заново, потому что разрабы спринга твари
                    updDocument = document.mutate().text(updDocText).id(new RandomIdGenerator().generateId()).build();
                }
                documentsWithTables.add(j, updDocument);
            }
            if (!remainder.isBlank()) {
                documentsWithTables.addAll(
                        handleRemainder(documentsWithTables.getLast(), remainder, remainderTokenCount, remainderTable)
                );
            }
            tmp = documentsWithTables;
        }
        return combineSmallChunks(tmp);
    }

    private List<Document> handleRemainder(Document document, String remainder, int remainderTokenCount, String remainderTable) {
        List<Document> remainderDocuments = new ArrayList<>();
        while (tokenCountEstimator.estimate(remainder) > MAX_INPUT_TOKEN_COUNT) {
            List<String> res = processTextWithBigMdTable(remainder, remainderTable);
            String chunkText = res.getFirst();
            remainder = res.getLast();
            if (tokenCountEstimator.estimate(remainder) >= remainderTokenCount) {
                // Неделимый remainder, не получится поделить красиво, делим как нибудь
                remainderDocuments.addAll(doSplit(remainder, MAX_INPUT_TOKEN_COUNT).stream()
                        .map(text -> document.mutate().text(text).id(new RandomIdGenerator().generateId()).build())
                        .toList());
                return remainderDocuments;
            }
            remainderTokenCount = tokenCountEstimator.estimate(remainder);
            Document remainderDocument = document.mutate().text(chunkText).id(new RandomIdGenerator().generateId()).build();
            remainderDocuments.add(remainderDocument);
        }
        Document remainderDocument = document.mutate().text(remainder).id(new RandomIdGenerator().generateId()).build();
        remainderDocuments.add(remainderDocument);
        return remainderDocuments;
    }

    private List<Document> combineSmallChunks(List<Document> documentsWithTables) {
        Document prevDocument = documentsWithTables.getFirst();
        List<Document> resultList = new ArrayList<>();
        resultList.add(prevDocument);
        for (int i = 1; i < documentsWithTables.size(); i++) {
            Document curDocument = documentsWithTables.get(i);
            String combinedText = prevDocument.getText() + curDocument.getText();
            if (tokenCountEstimator.estimate(combinedText)
                    < MAX_INPUT_TOKEN_COUNT) {
                resultList.removeLast();
                curDocument = prevDocument.mutate().text(combinedText).id(new RandomIdGenerator().generateId()).build();
            }
            resultList.add(curDocument);
        }
        return resultList;
    }

    private List<String> processTextWithBigMdTable(String documentText, String tableText) {
        if (tokenCountEstimator.estimate(documentText) < MAX_INPUT_TOKEN_COUNT) {
            return List.of(documentText, "");
        }
        String tmp = documentText;
        StringBuilder remainder = new StringBuilder();
        String[] rows = tableText.split("\n");
        String[] documentTextRows = tmp.split("\n");
        int index = documentTextRows.length - 1;
        while (tokenCountEstimator.estimate(tmp) > MAX_INPUT_TOKEN_COUNT) {
            String row = documentTextRows[index];
            tmp = tmp.replace(row.trim(), "").trim();
            index--;
            remainder.insert(0, row + "\n");
        }
        remainder.insert(0, rows[0] + "\n" + rows[1] + "\n");
        return List.of(tmp, remainder.toString());
    }
}
