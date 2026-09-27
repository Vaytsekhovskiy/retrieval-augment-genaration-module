package org.vced.convertmicroservice.watcher;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class FilePathPollingService extends FilePathAbstractService {
    private static final Map<String, FileAlterationMonitor> MONITORS = new ConcurrentHashMap<>();

    @SneakyThrows
    public Map<String, CompletableFuture<Void>> processRootPaths(List<String> rootPaths, boolean deleted) {
        Map<String, CompletableFuture<Void>> result = new HashMap<>();
        String[] normalizedRootExcepts = getExceptPaths()
                .stream()
                .map(rootExcept -> {
                    rootExcept = Path.of(rootExcept).toAbsolutePath().toString();
                    rootExcept = rootExcept.replace("\\", "/");
                    return rootExcept;
                })
                .toArray(String[]::new);
        for (String rootPath : rootPaths) {// Получаем все файлы рекурсивно в корневых директориях
            Collection<File> files = FileUtils.listFiles(
                    new File(rootPath), // Директория для поиска
                    TrueFileFilter.INSTANCE, // Фильтр для файлов (принимаем все файлы)
                    TrueFileFilter.INSTANCE  // Фильтр для директорий (рекурсивный обход)
            );
            // добавление всех найденных файлов в директории с флагом CREATED
            // или DELETED (если папки помечены флагом DELETED)
            files.stream()
                    .filter(file -> {
                        String normalizedFilePath = file.getAbsolutePath().replace("\\", "/");
                        for (String normalizedRootExcept : normalizedRootExcepts) {
                            if (normalizedFilePath.startsWith(normalizedRootExcept + "/")) {
                                // Помечаем все файлы из RootPathExcept как удалённые
                                // на случай если мы обновили список RootPathsExcept
                                processFile(file.getAbsolutePath(), FILE_STATE.DELETED);
                                return false;
                            }
                        }
                        return true;
                    })
                    .forEach(file -> {
                        // Если выбрана опция полной индексации папки, добавляем файлы в папке с флагом CREATED
                        // Если для папок выставлен флаг deleted, добавляем файлы в папке с флагом DELETED
                        if (full_folder_indexing || deleted) {
                            result.put(file.getAbsolutePath(),
                                    processFile(file.getAbsolutePath(), deleted ?
                                            FILE_STATE.DELETED :
                                            FILE_STATE.CREATED));
                        }
                    });
        }

        for (String rootPath : rootPaths) {
            stopRootPathMonitoring(rootPath);
            // Если папки не помечены флагом deleted, добавляем монитор для каждой папки
            if (!deleted) {
                MONITORS.put(rootPath, startRootPathMonitoring(rootPath));
            }
        }
        return result;
    }

    private CompletableFuture<Void> processFile(String absoluteFilePath, FILE_STATE fileState) {
        putConversionState(absoluteFilePath, CONVERSION_STATE.WAITING);
        log.info("file: {} was {}", absoluteFilePath, fileState);
        //kafkaProducer.sendMessage(absolutePath);
        AbstractMap.SimpleEntry<String, FILE_STATE> entry =
                new AbstractMap.SimpleEntry<>(absoluteFilePath, fileState);
        return kafkaProducer.sendMessage(entry);
    }

    private FileAlterationMonitor startRootPathMonitoring(String rootPath) {
        // Создаем наблюдателей за директориями
        FileAlterationObserver observer = getFileAlterationObserver(rootPath);

        // Настраиваем мониторинг с интервалом 1 секунда
        FileAlterationMonitor monitor = new FileAlterationMonitor(1000, observer);

        // Запускаем мониторинг
        try {
            monitor.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return monitor;
    }

    private void stopRootPathMonitoring(String rootPath) {
        if (MONITORS.containsKey(rootPath)) {
            try {
                MONITORS.get(rootPath).stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                MONITORS.remove(rootPath);
            }
        }
    }

    private FileAlterationObserver getFileAlterationObserver(String rootPath) {
        IOFileFilter filter = FileFilterUtils
                .notFileFilter(FileFilterUtils.or(
                        getExceptPaths()
                                .stream()
                                .map(FileFilterUtils::prefixFileFilter)
                                .toArray(IOFileFilter[]::new)
                ));
        FileAlterationObserver observer = new FileAlterationObserver(new File(rootPath), filter);
        observer.addListener(new FileAlterationListener() {
            @Override
            public void onStart(FileAlterationObserver observer) {
            }

            @Override
            public void onDirectoryCreate(File directory) {
            }

            @Override
            public void onDirectoryChange(File directory) {
            }

            @Override
            public void onDirectoryDelete(File directory) {
            }

            @Override
            public void onFileCreate(File file) {
                processFile(file.getAbsolutePath(), FILE_STATE.CREATED);
            }

            @Override
            public void onFileChange(File file) {
                processFile(file.getAbsolutePath(), FILE_STATE.MODIFIED);
            }

            @Override
            public void onFileDelete(File file) {
                processFile(file.getAbsolutePath(), FILE_STATE.DELETED);
            }

            @Override
            public void onStop(FileAlterationObserver observer) {
                //System.out.println("Monitoring stopped on " + observer.getDirectory());
            }
        });
        return observer;
    }
}
