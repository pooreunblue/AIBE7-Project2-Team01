package org.example.link.domain.portfolio.repository;

import org.example.link.domain.portfolio.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Long> {
    List<PortfolioEntity> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );
}
