package org.example.link.domain.request.dto;

import java.util.UUID;

import org.example.link.domain.request.entity.RequestPostFileEntity;

public record RequestPostFileResponse(
        UUID requestPostFileId,
        String originalFileName,
        String fileUrl,
        String contentType,
        Long fileSize,
        boolean thumbnail
) {
    public static RequestPostFileResponse from(RequestPostFileEntity file) {
        return new RequestPostFileResponse(
                file.getId(),
                file.getOriginalFileName(),
                file.getFileUrl(),
                file.getContentType(),
                file.getFileSize(),
                file.isThumbnail()
        );
    }
}
