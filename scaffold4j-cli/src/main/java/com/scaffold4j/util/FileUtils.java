package com.scaffold4j.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {

    private FileUtils() {}

    public static void createDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    public static void writeFile(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    public static void writeFileIfNotEmpty(Path file, String content) throws IOException {
        if (content != null && !content.isBlank()) {
            writeFile(file, content);
        }
    }
}
