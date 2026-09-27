package org.vced.convertmicroservice.watcher;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.vced.convertmicroservice.converter.KafkaProducer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Service
@PropertySource("classpath:application.yml")
@Slf4j
public abstract class FilePathAbstractService {
    @Autowired
    protected KafkaProducer kafkaProducer;
    @Autowired
    private FilePathRepository filePathRepository;
    @Value("${main.file.root.path}")
    private CopyOnWriteArrayList<String> selectedOnStartupRootPaths;
    @Value("${main.file.root.except}")
    protected CopyOnWriteArrayList<String> selectedOnStartupRootPathsExcept;
    @Value("${main.file.full-folder-indexing}")
    protected boolean full_folder_indexing;
    @Getter
    @Value("${main.mount-point}")
    protected String mountPoint;
    @Value("${main.file.full-folder-indexing-by-kafka}")
    protected boolean full_folder_indexing_by_kafka;
    private final static Map<String, CONVERSION_STATE> CONVERSION_STATE_MAP = new ConcurrentHashMap<>();
    @Getter
    private final static ConversionStatePublisher publisher = new ConversionStatePublisher();

    public static Map<String, CONVERSION_STATE> getConversionStateMap() {
        return new HashMap<>(CONVERSION_STATE_MAP);
    }

    public abstract Map<String, CompletableFuture<Void>> processRootPaths(List<String> rootPaths, boolean deleted);

    public static void putConversionState(String documentPath, CONVERSION_STATE conversionState) {
        CONVERSION_STATE_MAP.put(documentPath, conversionState);
        publisher.publish(getConversionStateMap());
    }

    public static void removeConversionState(String documentPath) {
        CONVERSION_STATE_MAP.remove(documentPath);
        publisher.publish(getConversionStateMap());
    }

    public List<String> getRootPaths() {
        return Stream.concat(
                        filePathRepository.findAll().stream()
                                .filter(filePath -> !filePath.isPathExcept())
                                .map(FilePath::getPath),
                        selectedOnStartupRootPaths.stream())
                // Чтобы можно было записывать относительные пути:
                .map(rootPath -> Path.of(rootPath).toAbsolutePath().toString())
                .distinct()
                .toList();
    }

    public List<String> getExceptPaths() {
        return Stream.concat(
                        filePathRepository.findAll().stream()
                                .filter(FilePath::isPathExcept)
                                .map(FilePath::getPath),
                        selectedOnStartupRootPathsExcept.stream())
                // Чтобы можно было записывать относительные пути:
                .map(rootPath -> Path.of(rootPath).toAbsolutePath().toString())
                .distinct()
                .toList();
    }

    public Map<String, CompletableFuture<Void>> addRootPath(String path) throws IllegalArgumentException {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("The given path is blank");
        }
        if (!path.startsWith(mountPoint)) {
            path = mountPoint + path;
        }
        if (!getRootPaths().contains(path)) {
            if (getExceptPaths().contains(path)) {
                throw new IllegalArgumentException(String
                        .format("The given path: %s is already contained in the except path", path));
            }
            Path addedPath = Path.of(path);
            getRootPaths().forEach(rootPath -> {
                if (addedPath.startsWith(Path.of(rootPath)))
                    throw new IllegalArgumentException(String
                            .format("The given path: %s is nested path for existing path: %s", addedPath, rootPath));
                if (Path.of(rootPath).startsWith(addedPath))
                    throw new IllegalArgumentException(String
                            .format("The existing path: %s is nested path for given path: %s", rootPath, addedPath));
            });
            if (Files.exists(addedPath) && Files.isDirectory(addedPath)) {
                filePathRepository.save(FilePath.builder().path(path).pathExcept(false).build());
                return processRootPaths(List.of(path), false);
            } else {
                throw new IllegalArgumentException(String.format("The given path: %s is not a valid path", path));
            }
        }
        return Map.of();
    }
    public Map<String, CompletableFuture<Void>> addRootPathExcept(String path) throws IllegalArgumentException {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("The given path is blank");
        }
        if (!path.startsWith(mountPoint)) {
            path = mountPoint + path;
        }
        if (!getExceptPaths().contains(path)) {
            if (getRootPaths().contains(path)) {
                throw new IllegalArgumentException(String
                        .format("The given path: %s is already contained in the except path", path));
            }
            Path addedPath = Path.of(path);
            getExceptPaths().forEach(rootPath -> {
                if (addedPath.startsWith(Path.of(rootPath)))
                    throw new IllegalArgumentException(String
                            .format("The given path: %s is nested path for existing path: %s", addedPath, rootPath));
                if (Path.of(rootPath).startsWith(addedPath))
                    throw new IllegalArgumentException(String
                            .format("The existing path: %s is nested path for given path: %s", rootPath, addedPath));
            });
            if (Files.exists(addedPath) && Files.isDirectory(addedPath)) {
                filePathRepository.save(FilePath.builder().path(path).pathExcept(true).build());
                return processRootPaths(getRootPaths(), false);
            } else {
                throw new IllegalArgumentException(String.format("The given path: \"%s\" is not a valid path", path));
            }
        }
        return Map.of();
    }
    @Transactional
    public void removePath(String path, boolean isExceptFolder) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("The given path is blank");
        }
        if (!path.startsWith(mountPoint)) {
            path = mountPoint + path;
        }
        selectedOnStartupRootPaths.remove(path);
        selectedOnStartupRootPathsExcept.remove(path);
        if (filePathRepository.existsByPath(path)) {
            filePathRepository.deleteFilePathByPath(path);
        }
        if (!isExceptFolder)
            processRootPaths(List.of(path), true);
        else processRootPaths(getRootPaths(), false);
    }

    public enum CONVERSION_STATE {
        WAITING,
        PROCESSING,
    }

    public enum FILE_STATE {
        CREATED,
        MODIFIED,
        DELETED
    }

    // FilePathService HAS A ConversionStatePublisher (Композиция)
    public static class ConversionStatePublisher {
        // Список всех активных подключений браузеров
        private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

        // Вызывается когда браузер переходит на апи. Постоянное соединение
        public SseEmitter subscribe() {
            SseEmitter emitter = new SseEmitter(0L); // без таймаута
            emitters.add(emitter); // добавляем браузер в список рассылки

            emitter.onCompletion(() -> emitters.remove(emitter)); // Браузер закрыл вкладку
            emitter.onTimeout(() -> emitters.remove(emitter)); // Отвалился интернет
            emitter.onError(e -> emitters.remove(emitter)); // Произошла ошибка

            return emitter;
        }

        // Вызывается при изменении CONVERSION_STATE_MAP
        public void publish(Map<String, CONVERSION_STATE> state) {
            // Проходим по всем клиентам
            for (SseEmitter emitter : emitters) {
                try {
                    // Шлём данные каждому
                    emitter.send(state);
                } catch (IOException | IllegalStateException e) {
                    emitters.remove(emitter);
                }
            }
        }
    }
}