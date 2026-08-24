package org.example.link.domain.trade.repository;

import org.example.link.domain.trade.entity.TradeEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
}