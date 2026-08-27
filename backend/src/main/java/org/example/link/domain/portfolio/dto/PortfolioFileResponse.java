package org.example.link.domain.portfolio.dto;

import org.example.link.domain.portfolio.entity.PortfolioFileEntity;

public record PortfolioFileResponse(
        Long portfolioFileId,
        String originalFileName,
        String fileUrl,
        String contentType,
        Long fileSize,
        boolean thumbnail
) {

    public static PortfolioFileResponse from(
            PortfolioFileEntity file
    ) {
        return new PortfolioFileResponse(
                file.getId(),
                file.getOriginalFileName(),
                file.getFileUrl(),
                file.getContentType(),
                file.getFileSize(),
                file.isThumbnail()
        );
    }
}
