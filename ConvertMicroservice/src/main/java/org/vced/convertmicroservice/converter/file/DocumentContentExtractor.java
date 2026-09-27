package org.vced.convertmicroservice.converter.file;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.vced.convertmicroservice.converter.PythonExecutor;
import org.vced.convertmicroservice.watcher.FilePathAbstractService;
import org.vced.ragutillib.document.CustomImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
@PropertySource("classpath:application.yml")
public abstract class DocumentContentExtractor {
    @Value("${main.file.pdf-root}")
    protected String pdfRoot;
    @Value("${main.file.image-root}")
    private String imageRoot;
    @Value("${main.image-filter}")
    private boolean imageFilter;
    @Autowired
    private FilePathAbstractService filePathAbstractService;
    public static final String TABLE_INDEX = "IDX";
    // Без пробела, т.к. разбиватель чанков на них тригерится
    // ПОЯСНЕНИЕ, в каждом маркере закодирован индекс таблицы, к которой этот маркер принадлежит
    // Этот индекс нужно парсить самостоятельно
    public static final String TABLE_REMOVED_MARKER = "TABLE" + TABLE_INDEX + "REMOVED";
    public static final Pattern TABLE_REMOVED_MARKER_REGEX = Pattern.compile("TABLE\\d+REMOVED");
    /*
        В эту переменную указываем, где будут лежать файлы без таблиц, ищем эту директорию на основе pdfRoot.
        Т.к. свойство @Value("${main.file.pdf-root}") инициализирует переменную pdfRoot только при создании бина,
        до этого момента в pdfRoot записан null. Следовательно, мы никак не сможем инициализировать docDirectoryNoTables
        до инициализации бина, следовательно инициализируем docDirectoryNoTables в рантайме в каком-нибудь методе
     */
    public static String DOC_DIRECTORY_NO_TABLES;

    @PostConstruct
    public void init() {
        DOC_DIRECTORY_NO_TABLES = Path.of(pdfRoot).getParent() + "/custom";
        DOC_DIRECTORY_NO_TABLES = Path.of(DOC_DIRECTORY_NO_TABLES).toAbsolutePath().toString();
        try {
            Files.createDirectories(Path.of(DOC_DIRECTORY_NO_TABLES));
        } catch (IOException e) {
            log.error("incorrect directory: {} (for documents without tables): {}",
                    DOC_DIRECTORY_NO_TABLES, e.getMessage());
        }
    }

    public static String replaceTableMarkersToTables(String text, List<String> markdownTables) {
        // вставляем красивые таблицы в текст
        for (int i = 0; i < markdownTables.size(); i++) {
            String table = markdownTables.get(i);
            text = replaceTableMarkerToTable(text, table, i);
        }
        return text;
    }
    /**
     * Удаляет ненужные строки без информации из текстовых таблиц. Например, удалит подобные строки:
     * |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
     * |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
     *
     * @param text - текст в котором может потенциально содержаться текстовая таблица
     * @return Тот же текст, но с удалёнными ненужными строками
     */
    public static String fixStringTable(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (!line.trim().matches("[\\s|]*") || line.isBlank())
                result.append(line).append("\n");
        }
        return result.toString().trim();
    }
    public static String replaceTableMarkerToTable(String text, String markdownTable, int idx) {
        // вставляем красивую таблицу в текст
        String rightMarker = getIdxTableMarker(idx);
        text = text.replace(rightMarker, "\n" + markdownTable);
        return text;
    }

    public static String getIdxTableMarker(int idx) {
        return TABLE_REMOVED_MARKER.replace(TABLE_INDEX, String.valueOf(idx));
    }

    public List<CustomImage> extractImages(String pdfFilePath, String pdfPathWithoutRoot) {
        List<CustomImage> resultList = new ArrayList<>();
        try {
            File file = new File(pdfFilePath);
            log.info("Processing {}", pdfFilePath);
            String fileNameWithoutExt = file.getName();
            PDDocument document = Loader.loadPDF(file);
            ImageExtractor extractor = new ImageExtractor();
            for (int i = 0; i < document.getPages().getCount(); i++) {
                PDPage page = document.getPage(i);
                extractor.IMAGE_BUFFER.clear();
                extractor.clearPositions();
                extractor.processPage(page);
                List<BufferedImage> imagesFromPage = extractor.IMAGE_BUFFER;
                List<Float> imageYPositions = extractor.getImageYPositions();
                for (int j = 0; j < imagesFromPage.size(); j++) {
                    var bImage = imagesFromPage.get(j);

                    int width = bImage.getWidth();
                    int height = bImage.getHeight();
                    boolean isTooSmall = width < 150 || height < 150;
                    boolean isWeirdAspectRatio = (width > height * 10) || (height > width * 10);
                    boolean shouldFilter = isTooSmall || isWeirdAspectRatio;

                    String imagePath = imageRoot + pdfPathWithoutRoot + '/' + fileNameWithoutExt + '/' + i + "_" + j + ".png";
                    File imageFile =
                            new File(imagePath);
                    imageFile.getParentFile().mkdirs();
                    ImageIO.write(bImage, "PNG", imageFile);

                    Boolean isStrange = null;
                    if (shouldFilter) {
                        isStrange = true;
                    } else if (imageFilter) {
                        String pythonFile = "ImageClassifier.py";
                        String pythonOutput = PythonExecutor.executePythonFile(
                                pythonFile,
                                imagePath
                        );
                        if (pythonOutput.isBlank()) {
                            log.error("Exception inside {} file. Python execution aborted.", pythonFile);
                        } else isStrange = pythonOutput.equals("0");
                    }
                    resultList.add(CustomImage.builder()
                            .path(imagePath)
                            .page(i+1) // в остальном коде (и много где в pdfbox (но не везде!)) нумерация страниц в pdf с 1, не нарушаем
                            .yPos(imageYPositions.get(j))
                            .isStrange(isStrange)
                            .build());
                }
            }
            document.close();
        } catch (IOException e) {
            log.error("Incorrect file path {}", e.getMessage());
            e.printStackTrace();
        }
        return resultList;
    }

    // Возвращает новый документ без таблиц (с маркерами) + список таблиц в md формате
    public abstract Map.Entry<File, List<String>> extractTables(String documentPath, String outputPdfFile);

    public static String extractText(File pdfFile) {
        try {
            PDDocument document = Loader.loadPDF(pdfFile);
            return new PDFTextStripper().getText(document);
        } catch (Exception e) {
            log.error("pdf pdfFile {} not found", pdfFile.getAbsolutePath());
            return "";
        }
    }

    protected abstract DocumentType getDocumentType();

    protected String getDocumentPathInsideNoTablesDir(String documentPath, String extension) {
        if (!extension.startsWith(".")) extension = "." + extension;
        extension = extension.toLowerCase();
        String rootPath = Path.of(filePathAbstractService.getRootPaths()
                        .stream()
                        .filter(documentPath::contains)
                        .findFirst()
                        .orElse(""))
                .toAbsolutePath()
                .toString();
        String pathWithoutRoot = Path.of(documentPath.replace(rootPath, "")).getParent().toString();
        if (pathWithoutRoot.length() <= 1) pathWithoutRoot = "";
        return DOC_DIRECTORY_NO_TABLES + "/" + pathWithoutRoot + "/" + FilenameUtils.getBaseName(documentPath) + extension;
    }

}
