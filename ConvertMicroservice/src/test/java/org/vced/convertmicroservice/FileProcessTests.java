//package org.vced.convertmicroservice;
//
//import lombok.SneakyThrows;
//import org.apache.commons.io.FileUtils;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.vced.convertmicroservice.converter.ConverterService;
//import org.vced.convertmicroservice.converter.chunk.ChunkSplitter;
//import org.vced.convertmicroservice.converter.file.DocumentContentExtractor;
//import org.vced.convertmicroservice.converter.KafkaProducer;
//import org.vced.convertmicroservice.watcher.FileAbstractService;
//import org.vced.electronutillib.document.CustomDocument;
//import org.vced.electronutillib.document.CustomImage;
//import org.vced.electronutillib.document.DocumentService;
//import org.vced.electronutillib.vector.Chunk;
//import org.vced.electronutillib.vector.ChunkService;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import static org.awaitility.Awaitility.await;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.yml", properties = "spring.profiles.active=test")
//@ContextConfiguration(initializers = {TestInitializer.Initializer.class})
//@DirtiesContext
//@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
//public class FileProcessTests {
//    @Value("${main.file.pdf-root}")
//    private String pdfRoot;
//    @Value("${main.file.image-root}")
//    private String imageRoot;
//    @Value("${main.file.root.path}")
//    private String[] rootPaths;
//    @Value("${main.file.root.except}")
//    private String[] rootExcepts;
//    @Value("${spring.ai.embedding.transformer.tokenizer.options.modelMaxLength}")
//    private int modelMaxLength;
//    @Autowired
//    private DocumentService documentService;
//    @Autowired
//    private ChunkService chunkService;
//    @Autowired
//    private ConverterService converterService;
//    @MockitoBean
//    private KafkaProducer kafkaProducer;
//
//    @AfterEach
//    public void destroyAll() throws IOException {
//        documentService.deleteAll();
//        chunkService.deleteAllChunks();
//        // delete all files from test folders except test-dir
//        FileUtils.cleanDirectory(Path.of(pdfRoot).toAbsolutePath().toFile());
//        FileUtils.cleanDirectory(Path.of(imageRoot).toAbsolutePath().toFile());
//        FileUtils.cleanDirectory(Path.of(DocumentContentExtractor.DOC_DIRECTORY_NO_TABLES).toAbsolutePath().toFile());
//    }
//    @SneakyThrows
//    @Test
//    public void should_process_file_correctly() {
//        // этот финт я назвал гига...
//        long filesCount = Objects.requireNonNull(Arrays.stream(Objects.requireNonNull(
//                        Arrays.stream(rootPaths)
//                        .map(rootPath -> new File(rootPath).listFiles()).filter(Objects::nonNull)
//                        .flatMap(Arrays::stream)
//                                .toArray(File[]::new)
//                ))
//                .filter(file -> {
//                    for (String except : rootExcepts) {
//                        if (file.getAbsolutePath().contains(except)) return false;
//                    }
//                    return true;
//                }))
//                .count();
//        List<Map.Entry<String, FileAbstractService.FILE_STATE>> files = new ArrayList<>();
//        await().atMost(120, TimeUnit.SECONDS).untilAsserted(() -> {
//            verify(kafkaProducer, times((int) filesCount)).sendMessage(argThat((arg) -> {
//                files.add(arg);
//                return arg.getValue().equals(FileAbstractService.FILE_STATE.CREATED);
//            }));
//        });
//        files.stream()
//                .map(KafkaProducer::sentEntry2json)
//                .forEach(converterService::receiveDocument);
//        // Check documents
//        List<CustomDocument> documents = documentService.findAllDocuments();
//        assertEquals(filesCount, documents.size());
//        documents.forEach(document -> {
//            document.getContent().forEach(content -> assertFalse(content.isBlank()));
//            assertTrue(Path.of(document.getPath()).toFile().exists());
//            assertTrue(Path.of(document.getOriginalPath()).toFile().exists());
//            assertNotNull(document.getId());
//            assertNotNull(document.getName());
//        });
//        // Check non strange document
//        CustomDocument nonStrangeDoc = documentService.getDocumentByName("ИП 7.2.1 Маркетинг.pdf");
//        // Check images from non-strange document
//        List<CustomImage> imagesFromNonStrangeDoc = nonStrangeDoc.getImages();
//        assertEquals(2, imagesFromNonStrangeDoc.size());
//        imagesFromNonStrangeDoc.forEach(image -> {
//            assertNotNull(image.getIsStrange()); // Если здесь ошибка, то
//            // 1. Либо у тебя нет python3.exe (см. README)
//            // 2. Либо у тебя нет файла ImageClassifier.py (см. main ветку проекта)
//            // 3. Либо другая причина, связанная с питоном или классификацией картинок
//            assertTrue(Path.of(image.getPath()).toFile().exists());
//        });
//        // Check related documents to non-strange document
//        Set<Long> relatedDocs = nonStrangeDoc.getRelatedDocs();
//        assertEquals(1, relatedDocs.size());
//        relatedDocs.forEach(id -> {
//            CustomDocument relatedDoc = documentService.getDocument(id);
//            assertNotNull(relatedDoc);
//            assertTrue(Path.of(relatedDoc.getPath()).toFile().exists());
//        });
//        // Check vector store
//        // Check all chunks
//        List<Chunk> chunks = chunkService.getAllChunks();
//        chunks.forEach(chunk -> {
//            assertNotNull(chunk);
//            assertNotNull(chunk.getId());
//            assertNotNull(chunk.getContent());
//            assertNotNull(chunk.getEmbedding());
//            assertNotNull(chunk.getMetadata());
//            // Проверяем, что длина каждого чанка меньше максимальной длины чанков у модели
//            assertTrue(ChunkSplitter.tokenCountEstimator.estimate(chunk.getContent()) <= modelMaxLength);
//        });
//        // Check chunks metadata
//        List<Chunk> foundChunksByFileName =
//                chunkService
//                        .getChunkByMetadataAttribute
//                                ("file_name", Path.of(nonStrangeDoc.getPath()).getFileName().toString());
//        List<Chunk> foundChunksById =
//                chunkService.getChunkByMetadataAttribute("id", String.valueOf(nonStrangeDoc.getId()));
//        assertEquals(foundChunksByFileName.size(), foundChunksById.size());
//        // Check if md tables logic
//        String nonStrangeContent = nonStrangeDoc.getContent().iterator().next();
//        assertTrue(nonStrangeContent.contains("""
//                | Система менеджмента качества | Система менеджмента качества | УТВЕРЖДАЮ Генеральный директор АО «НИПК «Электрон»  ____________________ А.М. Элинсон  «_______» __________________ 20__ г. |\s
//                | --- | --- | --- |
//                |  |  |\s
//                """));
//        assertFalse(DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(nonStrangeContent).find());
//        // Check chunks from .docx
//        String nonStrangeChunksContent = foundChunksByFileName
//                .stream()
//                .map(Chunk::getContent)
//                .collect(Collectors.joining());
//        assertTrue(nonStrangeChunksContent.contains("""
//                |  | ISO 9001: 2015, ГОСТ Р ИСО 9001-2015 | ISO 13485:2016. ГОСТ Р ИСО 13485-2017 |
//                | --- | --- | --- |
//                | Пункты стандартов | 8.2.2 | 7.2.1 |
//                """));
//        assertFalse(DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(nonStrangeChunksContent).find());
//        // Check .doc
//        CustomDocument nonStrangeDoc_doc = documentService.getDocumentByName(
//                "СТО 7.5.4-01 Защита комплектующих и продукции от воздействия электростатического разряда.pdf"
//        );
//        String nonStrange_docContent = nonStrangeDoc_doc.getContent().iterator().next();
//        assertTrue(nonStrange_docContent.contains("""
//                |  | УТВЕРЖДАЮ: Генеральный директор  АО «НИПК «Электрон»  ____________________ А.М.Элинсон  «_______» __________________ 20__ г. |\s
//                | --- | --- |
//                """));
//        assertFalse(DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(nonStrange_docContent).find());
//        // Check .xlsx
//        CustomDocument nonStrangeDoc_xlsx = documentService.getDocumentByName(
//                "Приложение Б.Таблица комплектности.pdf"
//        );
//        String nonStrange_xlsxContent = nonStrangeDoc_xlsx.getContent().iterator().next();
//        assertTrue(nonStrange_xlsxContent.contains("""
//                |---|---|---|---|---|---|---|
//                |Наименовние по РУ|||||||
//                |Код сборки|||||||
//                |РПУ|||||||
//                """));
//        assertFalse(DocumentContentExtractor.TABLE_REMOVED_MARKER_REGEX.matcher(nonStrange_xlsxContent).find());
//    }
//}
