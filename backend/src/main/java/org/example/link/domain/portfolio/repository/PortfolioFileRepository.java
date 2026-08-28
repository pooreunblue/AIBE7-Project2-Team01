package org.example.link.domain.portfolio.repository;

import java.util.UUID;

import org.example.link.domain.portfolio.entity.PortfolioFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioFileRepository
        extends JpaRepository<
                PortfolioFileEntity,
                UUID
                > {
    List<PortfolioFileEntity> findAllByPortfolioId(UUID portfolioId);
    List<PortfolioFileEntity> findAllByPortfolioIdOrderByIdAsc(UUID portfolioId);

    Optional<PortfolioFileEntity> findByPortfolioIdAndThumbnailTrue(
            UUID portfolioId
    );
}
