//package org.vced.botmicroservice;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Assertions;
//import org.springframework.ai.document.Document;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import lombok.extern.slf4j.Slf4j;
//import org.vced.electronutillib.document.CustomDocument;
//import org.vced.electronutillib.document.CustomImage;
//import org.vced.electronutillib.document.DocumentService;
//import org.vced.electronutillib.vector.ChunkService;
//import org.springframework.test.context.ContextConfiguration;
//import static org.vced.botmicroservice.CustomQuestionAnswerAdvisor.isImageInChunk;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(initializers = {TestContainerInitializer.Initializer.class})
//@Slf4j
//public class ImageProcessingTests extends TestContainerInitializer {
//
//    @Autowired
//    private DocumentService documentService;
//
//    @Autowired
//    private ChunkService chunkService;
//
//    @Autowired
//    private CustomQuestionAnswerAdvisor customQuestionAnswerAdvisor;
//
//    @BeforeEach
//    public void setUp() {
//        // Очистка перед каждым тестом
//        documentService.deleteAll();
//        chunkService.deleteAllChunks();
//    }
//
//    @Test
//    public void should_correctly_identify_images_in_single_page_chunk() {
//        // Тест граничных случаев для одностраничных чанков
//        CustomImage imageAtTop = CustomImage.builder().page(1).yPos(750.0f).build();
//        CustomImage imageAtBottom = CustomImage.builder().page(1).yPos(350.0f).build();
//        CustomImage imageAboveChunk = CustomImage.builder().page(1).yPos(900.0f).build();
//        CustomImage imageBelowChunk = CustomImage.builder().page(1).yPos(200.0f).build();
//
//        Document chunk = new Document("test", Map.of(
//                "page_number", 1,
//                "end_page_number", 1,
//                "position", 800.0f,
//                "end_position", 300.0f
//        ));
//
//        try {
//            assertTrue(isImageInChunk(imageAtTop, chunk));
//            assertTrue(isImageInChunk(imageAtBottom, chunk));
//            assertFalse(isImageInChunk(imageAboveChunk, chunk));
//            assertFalse(isImageInChunk(imageBelowChunk, chunk));
//        } catch (Exception e) {
//            Assertions.fail("Exception occurred: " + e.getMessage());
//        }
//    }
//
//    @Test
//    public void should_filter_images_by_chunk_position() {
//        CustomImage imageInChunk = CustomImage.builder()
//                .page(1)
//                .yPos(750.0f) // Внутри чанка (800-700)
//                .build();
//
//        CustomImage imageOutsideChunk = CustomImage.builder()
//                .page(1)
//                .yPos(650.0f) // Вне чанка
//                .build();
//
//        CustomDocument doc = CustomDocument.builder()
//                .name("testDoc")
//                .images(List.of(imageInChunk, imageOutsideChunk))
//                .content(Collections.singleton("test"))
//                .build();
//
//        documentService.saveDocument(doc);
//
//        // Чанк покрывает только первое изображение
//        Document chunk = Document.builder()
////                .id(java.util.UUID.randomUUID().toString())
//                .text("chunk covering imageInChunk")
//                .metadata("id", doc.getId())
//                .metadata("page_number", 1)
//                .metadata("end_page_number", 1)
//                .metadata("position", 800.0f)
//                .metadata("end_position", 700.0f)
//                .build();
//        chunkService.saveChunk(chunk);
//
//        boolean result1 = isImageInChunk(imageInChunk, chunk);
//        boolean result2 = isImageInChunk(imageOutsideChunk, chunk);
//        assertTrue(result1, "Image within chunk should be included");
//        assertFalse(result2, "Image outside chunk should be excluded");
//    }
//}
//
