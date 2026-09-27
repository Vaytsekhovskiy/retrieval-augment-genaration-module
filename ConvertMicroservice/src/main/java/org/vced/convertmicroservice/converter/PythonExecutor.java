package org.vced.convertmicroservice.converter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PythonExecutor {
    public static String executePythonFile(String pythonFile, String... args) {
        if (pythonFile == null || !pythonFile.endsWith(".py")) {
            log.error("Invalid python file");
            return "";
        }
        if (!Path.of(pythonFile).toFile().exists()) {
            log.error("Python file {} does not exist", pythonFile);
            return "";
        }
        String pythonOutput = "";
        List<String> command = new ArrayList<>();
        command.add("python3");
        command.add(pythonFile);
        command.addAll(List.of(args));
        try {
            Process proc = new ProcessBuilder().command(command).start();
            proc.waitFor();
            pythonOutput =
                    new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            proc.destroy();
        } catch (IOException | InterruptedException e) {
            log.error("python3 command does not exist");
        }
        return pythonOutput;
    }
}
