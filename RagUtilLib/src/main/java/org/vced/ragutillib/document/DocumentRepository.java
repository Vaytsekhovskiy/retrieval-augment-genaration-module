package org.vced.ragutillib.document;

import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Интерфейс прослойка между JAVA и SQL*/
@Repository
public interface DocumentRepository extends BaseDocumentRepository<CustomDocument> {
    Optional<CustomDocument> findDocumentByOriginalPath(String path);
    Optional<CustomDocument> findCustomDocumentByPath(String path);
    Optional<CustomDocument> findCustomDocumentByName(String name); // optional alias
}