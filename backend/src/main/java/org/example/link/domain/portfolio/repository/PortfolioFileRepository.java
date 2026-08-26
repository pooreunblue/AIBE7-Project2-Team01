package org.example.link.domain.portfolio.repository;

import org.example.link.domain.portfolio.entity.PortfolioFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioFileRepository
        extends JpaRepository<
                PortfolioFileEntity,
                Long
                > {
}
