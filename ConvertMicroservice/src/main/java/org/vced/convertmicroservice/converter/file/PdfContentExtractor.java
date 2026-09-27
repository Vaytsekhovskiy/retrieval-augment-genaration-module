package org.vced.convertmicroservice.converter.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PdfContentExtractor extends DocumentContentExtractor {
    @Override
    public Map.Entry<File, List<String>> extractTables(String documentPath, String outputPdfFile) {
        // На данный момент, исходя из всех известных мне способов
        // КОРРЕКТНО ОБРАБОТАТЬ ТАБЛИЦЫ В PDF НЕВОЗМОЖНО
        // TODO: Удачи
        return new AbstractMap.SimpleEntry<>(new File(outputPdfFile), List.of());
    }

    public static int extractPageNumber(String documentPath) {
        int pages = 0;
        try (PDDocument document = Loader.loadPDF(new File(documentPath))) {
            pages = document.getNumberOfPages();
        } catch (IOException e) {
            log.error("failed to load pdf: {}. Document Does not exist, page number is 0", documentPath);
        }
        return pages;
    }
    @Override
    protected DocumentType getDocumentType() {
        return DocumentType.PDF;
    }
}
