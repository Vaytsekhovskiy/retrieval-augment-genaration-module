package org.vced.botmicroservice;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.vced.botmicroservice.config.BotConfigService;
import org.vced.ragutillib.document.*;
import org.vced.ragutillib.orm.SuggestedDocumentORM;
import org.vced.ragutillib.vector.ChunkService;

import java.util.*;
import java.util.stream.Collectors;

/**@author  Егор
 * Сервис построенный на алгоритме Кнутта-Морриса-Пратта, для поиска нужных документов по ключевым словам
 * */
@Service
@RequiredArgsConstructor
@Import(value = {DocumentConfig.class})
@PropertySource("classpath:application.yml")
public class SearchService {
    private final ChunkService chunkService;
    private final DocumentService documentService;
    private final BotConfigService botConfigService;

    public List<SuggestedDocumentORM> getAppropriateFiles(String request, List<String> authorities) {
        return getAppropriateFilesByEmbeddingAlgorithm(request, authorities);
    }

    private List<SuggestedDocumentORM> getAppropriateFilesByEmbeddingAlgorithm(String request, List<String> authorities) {
        var searchRequest = SearchRequest.from(botConfigService.searchRequest())
                .query(request)
                .build();
        List<Document> store;
        if (botConfigService.isIgnoreAuthorities()) {
            store = chunkService.similaritySearch(searchRequest);
        } else {
            store = chunkService.similaritySearchWithAccess(searchRequest, authorities);
        }return store.stream()
                .map(document -> new SuggestedDocumentORM(
                        (int)document.getMetadata().get("id"), // почему-то здесь возвращается int вместо long
                        (String) document.getMetadata().get("file_name"),
                        document.getText(),
                        findRelatedDocuments((int) document.getMetadata().get("id")).stream()
                                .filter(Objects::nonNull)
                                .map(relatedDoc -> new SuggestedDocumentORM(
                                        relatedDoc.getId(),
                                        relatedDoc.getName()
                                        )
                                )
                                .collect(Collectors.toSet()),
                        (Integer)document.getMetadata().get("page_number")
                )
                )
                .toList();
    }

    private Set<BaseDocument> findRelatedDocuments(long id) {
        CustomDocument customDoc = documentService.getDocument(id);
        if (customDoc != null) {
            return new HashSet<>(documentService.findAllRelatedDocuments(id));
        }

        return Set.of();
    }
}
