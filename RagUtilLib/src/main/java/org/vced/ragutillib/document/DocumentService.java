package org.vced.ragutillib.document;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.vced.ragutillib.vector.ChunkService;

import java.io.FileNotFoundException;

@Service
@Import(value = {DocumentConfig.class})
public class DocumentService extends BaseDocumentService<CustomDocument, DocumentRepository> {
    // Constructor injection via super
    public DocumentService(DocumentRepository documentRepository, ChunkService chunkService) {
        super(documentRepository, chunkService);
    }

    /**
     * Find path for given file name using database
     */
    public String getPdfPath(long id) throws FileNotFoundException {
        CustomDocument doc = getDocument(id);
        if (doc == null) throw new FileNotFoundException();
        return doc.getPath();
    }

    public CustomDocument getDocumentByOriginalPath(String path) {
        return repository.findDocumentByOriginalPath(path).orElse(null);
    }

    public CustomDocument getDocumentByPath(String path) {
        return repository.findCustomDocumentByPath(path).orElse(null);
    }

    public CustomDocument getDocumentByName(String name) {
        return repository.findCustomDocumentByName(name).orElse(null);
    }
}
