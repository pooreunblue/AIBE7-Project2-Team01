package org.example.link.domain.trade.repository;

import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
    boolean existsByChatRoomId(Long chatRoomId);
    boolean existsByChatRoomIdAndStatusIn(Long chatRoomId, Collection<TradeStatus> statuses);
    Page<TradeEntity> findByPayerIdOrPayeeId(Long payerId, Long payeeId, Pageable pageable);
}