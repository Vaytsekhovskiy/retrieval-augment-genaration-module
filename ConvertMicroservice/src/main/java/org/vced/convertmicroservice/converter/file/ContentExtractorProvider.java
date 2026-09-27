package org.vced.convertmicroservice.converter.file;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContentExtractorProvider {
    private final Map<DocumentType, DocumentContentExtractor> type2extractor =
            new HashMap<>();
    public ContentExtractorProvider(List<DocumentContentExtractor> extractors) {
        extractors.forEach(extractor -> type2extractor.put(extractor.getDocumentType(), extractor));
    }
    public DocumentContentExtractor getDocumentTableExtractor(DocumentType documentType) {
        return type2extractor.get(documentType);
    }
}
