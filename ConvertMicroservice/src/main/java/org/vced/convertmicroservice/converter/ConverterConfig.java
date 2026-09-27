package org.vced.convertmicroservice.converter;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.vced.convertmicroservice.converter.file.*;
import org.vced.convertmicroservice.watcher.FilePathPollingService;
import org.vced.convertmicroservice.watcher.WatcherType;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
@Slf4j
public class ConverterConfig {
    private final DocxContentExtractor docxTablesExtractor;
    private final DocContentExtractor docTablesExtractor;
    private final PdfContentExtractor pdfTablesExtractor;
    private final XlsxContentExtractor xlsxTablesExtractor;
    @Bean
    ContentExtractorProvider documentTableProvider() {
        return new ContentExtractorProvider(List.of(
                docxTablesExtractor,
                docTablesExtractor,
                pdfTablesExtractor,
                xlsxTablesExtractor
        ));
    }

    LocalOfficeManager officeManager;
    @Bean
    @Order(1)
    CommandLineRunner connectToLibreOffice() {
        return _ -> {
            // Create an office manager using the default configuration.
            // The default port is 2002. Note that when an office manager
            // is installed, it will be the one used by default when
            // a converter is created.
            officeManager = LocalOfficeManager.builder().install().build();
            try {
                // Start an office process and connect to the started instance (on port 2002).
                officeManager.start();
            } catch (OfficeException | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
//            finally {
//                // Stop the office process
//                OfficeUtils.stopQuietly(officeManager);
//            }
        };
    }
    @PreDestroy
    public void cleanup() {
        if (officeManager != null) {
            OfficeUtils.stopQuietly(officeManager);
        }
    }
    @Value("${main.file.folder-watcher-type}")
    private WatcherType watcherType;
    /**CommandLineRunner - работает параллельно spring приложению*/
    @Bean
    @Order(2)
    CommandLineRunner checkNewFiles(FilePathPollingService pollingService) {
        return _ -> pollingService.processRootPaths(pollingService.getRootPaths(), false);
    }
}