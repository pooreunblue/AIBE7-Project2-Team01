package org.example.link.domain.portfolio.repository;

import org.example.link.domain.portfolio.entity.PortfolioFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioFileRepository
        extends JpaRepository<
                PortfolioFileEntity,
                Long
                > {
    List<PortfolioFileEntity> findAllByPortfolioId(Long portfolioId);
    List<PortfolioFileEntity> findAllByPortfolioIdOrderByIdAsc(Long portfolioId);

    Optional<PortfolioFileEntity> findByPortfolioIdAndThumbnailTrue(
            Long portfolioId
    );
}
