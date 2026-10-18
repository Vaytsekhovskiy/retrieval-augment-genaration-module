//package org.vced.botmicroservice;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.springframework.ai.document.Document;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.vced.electronutillib.document.CustomDocument;
//import org.vced.electronutillib.document.CustomImage;
//import org.vced.electronutillib.document.DocumentService;
//import org.vced.electronutillib.vector.ChunkService;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//
//public abstract class ApiTests {
//    @Autowired
//    private ChunkService chunkService;
//    @Autowired
//    private DocumentService documentService;
//
//    @BeforeEach
//    public void setUp() {
//        CustomDocument relatedDocument = CustomDocument.builder()
//                .name("relatedDocument")
//                .path("relatedDocument.pdf")
//                .originalPath("relatedDocument.docx")
//                .isStrange(true)
//                .build();
//        documentService.saveDocument(relatedDocument);
//        CustomDocument customDocument = CustomDocument.builder()
//                .name("testDocument")
//                .path("testDocument.pdf")
//                .originalPath("testDocument.docx")
//                .isStrange(false)
//                .relatedDocs(Set.of(documentService.getDocumentByOriginalPath(relatedDocument.getOriginalPath()).getId()))
//                .content(Collections.singleton("testDocument"))
//                .images(List.of(CustomImage.builder().path("image").page(1).isStrange(false).build()))
//                .build();
//        documentService.saveDocument(customDocument);
//        Document document = Document.builder()
//                .text("testDocument")
//                .metadata(Map.of("id", documentService
//                                .getDocumentByOriginalPath(customDocument.getOriginalPath()).getId(),
//                        "file_name", "testDocument",
//                        "page_number", 1))
//                .build();
//        chunkService.saveChunk(document);
//
//    }
//    @AfterEach
//    public void tearDown() {
//        chunkService.deleteAllChunks();
//        documentService.deleteAll();
//    }
//}
