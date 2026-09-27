package org.vced.convertmicroservice.converter.file;

import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.LocalConverter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Component
public class DocContentExtractor extends DocumentContentExtractor {

    @Override
    public Map.Entry<File, List<String>> extractTables(String documentPath, String outputPdfFile) {
        Path createdDocxPath = Path.of(getDocumentPathInsideNoTablesDir(documentPath, ".docx"));
        try {
            Files.deleteIfExists(createdDocxPath);
            final DocumentFormat format
                    = DocumentFormat.builder()
                    .from(DefaultDocumentFormatRegistry.DOCX)
                    .build();

            LocalConverter
                    .make()
                    .convert(new FileInputStream(documentPath))
                    .as(Objects.requireNonNull(DefaultDocumentFormatRegistry.getFormatByMediaType("application/msword")))
                    .to(new File(createdDocxPath.toUri()))
                    .as(format)
                    .execute();
            var result = DocxContentExtractor.cutTablesFromDocx(createdDocxPath);
            Files.delete(createdDocxPath);
            return result;
        } catch (OfficeException ex) {
            log.error("LibreOffice is not installed", ex);
        } catch (FileNotFoundException ex) {
            log.error("Incorrect document path: {}. The system won't process document tables", documentPath, ex);
        } catch (IOException e) {
            log.error("Incorrect document path: {}. The system won't process document tables"
                    , createdDocxPath, e);
        }
        return new AbstractMap.SimpleEntry<>(new File(outputPdfFile), List.of());
    }

    @Override
    protected DocumentType getDocumentType() {
        return DocumentType.DOC;
    }
}
