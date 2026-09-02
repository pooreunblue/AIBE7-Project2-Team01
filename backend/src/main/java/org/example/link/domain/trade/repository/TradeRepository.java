package org.example.link.domain.trade.repository;

import jakarta.persistence.LockModeType;
import java.util.UUID;

import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TradeEntity t where t.id = :tradeId")
    Optional<TradeEntity> findByIdForUpdate(@Param("tradeId") UUID tradeId);

    boolean existsByChatRoomId(UUID chatRoomId);
    boolean existsByChatRoomIdAndStatusIn(UUID chatRoomId, Collection<TradeStatus> statuses);
    Page<TradeEntity> findByPayerIdOrPayeeId(UUID payerId, UUID payeeId, Pageable pageable);
}
