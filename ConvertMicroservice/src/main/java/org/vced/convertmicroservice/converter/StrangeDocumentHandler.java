package org.vced.convertmicroservice.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vced.ragutillib.document.CustomDocument;
import org.vced.ragutillib.document.DocumentService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class StrangeDocumentHandler {
    private final DocumentService documentService;
    /*
        Предполагается написать здесь конвейер, который будет пытаться разными способами обработать
        странные документы
        TODO:
            1. Попытаться добавить TOC
        2. Попытаться связать с не странным документом (таким образом это идёт как приложение)
        TODO:
         залезать внутрь не странного документа, и искать упоминания странного документа, если есть, добавлять
    */
    public void handeStrangeDocuments(Path updatedOriginalDir) {
        try (Stream<Path> filePaths = Files.walk(updatedOriginalDir)) {
            Set<Long> strangeDocsIds = new HashSet<>();
            List<CustomDocument> nonStrangeDocs = new ArrayList<>();
            filePaths
                    .filter(filePath -> filePath.getParent().equals(updatedOriginalDir))
                    .forEach(filePath -> {
                CustomDocument docFromDb = documentService
                        .getDocumentByOriginalPath(filePath.toString());
                if (docFromDb == null) return;
                if (docFromDb.isStrange())
                    strangeDocsIds.add(docFromDb.getId());
                else nonStrangeDocs.add(docFromDb);
            });
            nonStrangeDocs.forEach(document -> {
                document.setRelatedDocs(strangeDocsIds); // не добавляем к существующим
                // а заново обновляем коллекцию, т.к. в предыдущей могут быть удалённые
                documentService.updateDocument(document);
            });
        } catch (UncheckedIOException | IOException e) {
            /*
             FileSystemException который оборачивается в UncheckedIOException из-за того, что
             Stream<Path> filePaths = Files.walk(updatedOriginalDir);
             создаёт ленивый Stream<Path>, и само чтение файловой системы
             (и возможное исключение) произойдёт только при вызове терминальной операции, такой как .forEach(...).
             TODO: НОРМАЛЬНО обрабатывать это исключение
            */
            log.error("I/O error during directory scan ({}): {}", updatedOriginalDir, e.toString());
        }
    }
}
