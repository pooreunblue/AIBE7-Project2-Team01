package org.example.link.common.storage.dto;

public record StoredFile(
        String originalFileName,
        String storedFileName,
        String path,
        String url,
        String contentType,
        long fileSize
) {
}