package org.example.link.domain.request.dto;

import org.example.link.domain.request.entity.RequestPostFileEntity;

public record RequestPostFileResponse(
        Long requestPostFileId,
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
