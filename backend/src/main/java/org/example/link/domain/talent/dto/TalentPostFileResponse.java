package org.example.link.domain.talent.dto;

import java.util.UUID;

import org.example.link.domain.talent.entity.TalentPostFileEntity;

public record TalentPostFileResponse(
        UUID talentPostFileId,
        String originalFileName,
        String fileUrl,
        String contentType,
        Long fileSize,
        boolean thumbnail
) {
    public static TalentPostFileResponse from(TalentPostFileEntity file) {
        return new TalentPostFileResponse(
                file.getId(),
                file.getOriginalFileName(),
                file.getFileUrl(),
                file.getContentType(),
                file.getFileSize(),
                file.isThumbnail()
        );
    }
}
