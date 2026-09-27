package org.vced.ragutillib.document;

import org.vced.ragutillib.vector.ChunkService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseDocumentService<T extends BaseDocument, R extends BaseDocumentRepository<T>> {
    protected final R repository;
    private final ChunkService chunkService;

    protected BaseDocumentService(R repository, ChunkService chunkService) {
        this.repository = repository;
        this.chunkService = chunkService;
    }

    public T getDocument(long id) {
        return repository.findById(id).orElse(null);
    }

    public T getDocumentByName(String name) {
        return repository.findByName(name).orElse(null);
    }

    public T saveDocument(T document) {
        return repository.save(document);
    }

    public T updateDocument(T document) {
        return saveDocument(document);
    }

    public void deleteDocument(T document) {
        repository.delete(document);
    }

    public List<T> findAllDocuments() {
        return repository.findAll();
    }

    public List<DocumentStatsDTO> findAllDocumentsLimit(int page, int size) {
        if (page < 0 || size <= 0) return List.of();
        int offset = page * size;
        var result = repository.findAllLimit(size, offset);
        return result.stream().map(document ->
                DocumentStatsDTO.builder()
                        .name((String) document[1])
                        .documentType((String) document[2])
                        .totalChunks(chunkService.getChunkByMetadataAttribute("id", String.valueOf(document[0])).size())
                        .build()
        )
                .toList();
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public Set<T> findAllRelatedDocuments(long id) {
        T base = getDocument(id);
        if (base == null || base.getRelatedDocs() == null) return Set.of();
        return base.getRelatedDocs().stream()
                .map(this::getDocument)
                .filter(d -> d != null)
                .collect(Collectors.toSet());
    }

    public DocumentsStatsDTO getDocumentsStats() {
        var result = repository.findDocumentsStats().get(0);
        return DocumentsStatsDTO.builder()
                .totalFiles(((Number) result[0]).intValue())
                .build();
    }
}
