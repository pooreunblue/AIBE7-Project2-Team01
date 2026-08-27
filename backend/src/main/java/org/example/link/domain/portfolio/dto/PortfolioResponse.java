package org.example.link.domain.portfolio.dto;

import java.util.UUID;

import org.example.link.domain.portfolio.entity.PortfolioEntity;

import java.time.Instant;

public record PortfolioResponse(
        UUID portfolioId,
        UUID userId,
        String title,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static PortfolioResponse from(
            PortfolioEntity portfolio
    ) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getUser().getId(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }
}
