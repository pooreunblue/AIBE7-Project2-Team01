package org.example.link.domain.trade.repository;

import java.util.UUID;

import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {
    boolean existsByChatRoomId(UUID chatRoomId);
    boolean existsByChatRoomIdAndStatusIn(UUID chatRoomId, Collection<TradeStatus> statuses);
    Page<TradeEntity> findByPayerIdOrPayeeId(UUID payerId, UUID payeeId, Pageable pageable);
}