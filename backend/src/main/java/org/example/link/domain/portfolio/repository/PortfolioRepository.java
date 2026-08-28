package org.example.link.domain.portfolio.repository;

import java.util.UUID;

import org.example.link.domain.portfolio.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<PortfolioEntity, UUID> {
    List<PortfolioEntity> findByUserIdOrderByCreatedAtDesc(
            UUID userId
    );
}
