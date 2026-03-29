package com.homepage.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class FileUtils {

    public static LocalDateTime getCreationTime(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            log.error("Failed to get creation time for: {}", path, e);
            return null;
        }
    }

    public static String getMimeType(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            log.error("Failed to get mime type for: {}", path, e);
            return "application/octet-stream";
        }
    }

    public static boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public static boolean isTextFile(String mimeType) {
        return mimeType != null && (mimeType.startsWith("text/") || 
                mimeType.contains("json") || 
                mimeType.contains("xml") ||
                mimeType.contains("javascript"));
    }
}
