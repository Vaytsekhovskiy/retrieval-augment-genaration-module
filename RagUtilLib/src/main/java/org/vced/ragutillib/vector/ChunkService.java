package org.vced.ragutillib.vector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.vced.ragutillib.vector.AuthorityChunkFilters.buildAuthBasedFilter;
import static org.vced.ragutillib.vector.AuthorityChunkFilters.buildAccessFilter;

@Service
@RequiredArgsConstructor
public class ChunkService {
    private final ChunkRepository chunkRepository;
    @Getter
    private final VectorStore vectorStore;

    public List<Chunk> getChunkByMetadataAttribute(String key, String value) {
        return chunkRepository.findByAttribute(key, value);
    }
    public void deleteChunkByFileName(String fileName) {
        chunkRepository.deleteChunkByFileName(fileName);
    }
    public void deleteChunksByMetadataId(Long id) {
        chunkRepository.deleteChunksByMetadataId(id);
    }
    public void deleteAllChunks() {
        chunkRepository.deleteAll();
    }
    public List<Chunk> getAllChunks() {
        return chunkRepository.findAll();
    }
    public int updateAuthoritiesById(Long id, List<String> authorities) {
        return chunkRepository.updateAuthoritiesById(id, authorities.toArray(new String[0]));
    }
    public int incrementUsageCountByUuid(String uuid) {
        return chunkRepository.incrementUsageCountByUuid(uuid);
    }
    public List<Chunk> getTopChunksByUsageCount(int limit) {
        return chunkRepository.findTopChunksByUsageCount(limit);
    }
    public Document saveChunk(Document document) {
        vectorStore.add(List.of(document));
        return document;
    }
    public List<Document> saveChunks(List<Document> documents) {
        vectorStore.add(documents);
        return documents;
    }
    public List<Document> similaritySearch(String query) {
        return vectorStore.similaritySearch(query);
    }
    public List<Document> similaritySearch(SearchRequest searchRequest) {
        return vectorStore.similaritySearch(searchRequest);
    }

    public List<Document> similaritySearchWithFilter(String query, Filter.Expression filter) {
        var searchRequestToUse = SearchRequest.builder().query(query).filterExpression(filter).build();
        return vectorStore.similaritySearch(searchRequestToUse);
    }
    public List<Document> similaritySearchWithFilter(SearchRequest searchRequest, Filter.Expression filter) {
        var searchRequestToUse = SearchRequest.from(searchRequest).filterExpression(filter).build();
        return vectorStore.similaritySearch(searchRequestToUse);
    }

    public List<Document> similaritySearchWithAuthorities(String query, List<String> authorities) {
        return similaritySearchWithFilter(query, buildAuthBasedFilter(authorities));
    }
    public List<Document> similaritySearchWithAuthorities(SearchRequest searchRequest, List<String> authorities) {
        return similaritySearchWithFilter(searchRequest, buildAuthBasedFilter(authorities));
    }

    // supports both YT articles and regular documents
    public List<Document> similaritySearchWithAccess(String query, List<String> authorities) {
        return similaritySearchWithFilter(query, buildAccessFilter(authorities));
    }
    public List<Document> similaritySearchWithAccess(SearchRequest searchRequest, List<String> authorities) {
        return similaritySearchWithFilter(searchRequest, buildAccessFilter(authorities));
    }
}
