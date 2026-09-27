package org.vced.convertmicroservice.converter.access;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vced.ragutillib.document.CustomDocument;
import org.vced.ragutillib.document.DocumentService;
import org.vced.ragutillib.vector.ChunkService;

import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccessSyncService {

    private final AccessReader accessReader;
    private final DocumentService documentService;
    private final ChunkService chunkService;

    /**
     * Метод обновления ролей для всех документов и чанков
     * @return количество обновленных документов
     */
    @Transactional
    public int updateAuthoritiesForAll() {
        log.info("Started authorities sync for all documents");
        List<CustomDocument> documents = documentService.findAllDocuments();
        int updated = 0;
        int totalChunksUpdated = 0;

        for (CustomDocument doc : documents) {
            try {
                String path = doc.getOriginalPath();
                if (path == null || path.isBlank()) {
                    log.debug("Skip document {}: empty filePath", doc.getId());
                    continue;
                }

                List<String> acl = accessReader.getAcl(path);
                if (acl == null || acl.isEmpty()) {
                    // Избегаем затирания существующих прав, если не удалось получить ACL
                    log.debug("Skip document {}: empty ACL for path {}", doc.getId(), path);
                    continue;
                }

                List<String> newAuthorities = new ArrayList<>(acl);
                List<String> currentAuthorities = new ArrayList<>(
                        doc.getAuthorities() == null ? List.of() : doc.getAuthorities());

                // Сравниваем как множества, чтобы избежать ложных срабатываний из-за порядка
                if (!Objects.equals(new HashSet<>(currentAuthorities), new HashSet<>(newAuthorities))) {
                    doc.setAuthorities(newAuthorities);
                    doc = documentService.updateDocument(doc); // явное сохранение из-за маппинга @ElementCollection document_authority
                    updated++;

                    // Обновляем чанки
                    int chunksUpdated = chunkService.updateAuthoritiesById(
                            doc.getId(), newAuthorities);
                    totalChunksUpdated += chunksUpdated;
                }

                log.debug("Updated authorities for document {} ({} -> {})",
                        doc.getId(), currentAuthorities, newAuthorities);
            } catch (Exception ex) {
                log.warn("Failed to update authorities for document {}", doc.getId(), ex);
            }
        }

        log.info("Authorities sync finished. Documents updated: {}. Chunks updated: {}", updated, totalChunksUpdated);
        return updated;
    }
}