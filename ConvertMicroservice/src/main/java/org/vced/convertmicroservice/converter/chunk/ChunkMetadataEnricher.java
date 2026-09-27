package org.vced.convertmicroservice.converter.chunk;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ChunkMetadataEnricher {
    /*
        https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html
     */
    private final SummaryMetadataEnricher summaryEnricher;
    // На метод приходит ПАРАГРАФ!!!!
    public List<Document> enrichDocuments(List<Document> documents) {
        /*
        На данный момент не имеет смысла:
        documents = keywordEnricher.apply(documents);
         */
        return summaryEnricher.apply(documents);
    }
}
