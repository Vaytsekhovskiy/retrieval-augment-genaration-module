package org.vced.ragutillib.log;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/*
    Контроллер для получения логов, которые сохраняются в отдельных файлах
     согласно конфигурации ../resources/logback-spring.xml
 */
@RestController
public class LogController {
    @Value("${spring.application.name}")
    private String appName;
    private static final List<String> LOG_LEVELS =
            Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL");
    /* Для любителей клин кода, можно конечно ловить паттерн и проверять строки в контроллере согласно этому паттерну
    private String filePattern;

    @PostConstruct
    public void init() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        FileAppender<?> fileAppender = (FileAppender<?>) context.getLogger("ROOT").getAppender("FILE");

        if (fileAppender != null && fileAppender.getEncoder() instanceof PatternLayoutEncoder encoder) {
            filePattern = encoder.getPattern(); // оригинальный шаблон
        }
    }
     */

    @GetMapping("/api/${spring.application.name}/log/{level}")
    public ResponseEntity<Resource> getLog(@PathVariable String level) throws IOException {
        Path logPath = Paths.get(System.getProperty("user.dir"), "log.log");

        // Если такого уровня логов нет, читаем логи INFO и выше
        int minLevelIndex = !LOG_LEVELS.contains(level.toUpperCase()) ?
                LOG_LEVELS.indexOf("INFO") :
                LOG_LEVELS.indexOf(level.toUpperCase());

        List<String> filteredLines = Files.readAllLines(logPath, StandardCharsets.UTF_8)
                .stream()
                .filter(line -> {
                    boolean match = true;
                    for (int i = 0; i < minLevelIndex; i++) {
                        if (line.startsWith("[" + LOG_LEVELS.get(i))) {
                            match = false;
                            break;
                        }
                    }
                    return match;
                })
                .toList();
        ByteArrayResource resource = new ByteArrayResource(
                String.join("\n", filteredLines).getBytes(StandardCharsets.UTF_8)
        );
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + appName + "_" + level + ".log")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}
