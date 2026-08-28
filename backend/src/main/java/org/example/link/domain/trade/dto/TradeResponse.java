package org.example.link.domain.trade.dto;

import java.util.UUID;

import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(
        UUID tradeId,
        UUID chatRoomId,
        UUID requestPostId,
        UUID talentPostId,
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        TradeStatus status,
        Instant paidAt,
        Instant completedAt,
        Instant cancelledAt,
        Instant createdAt
) {
    public static TradeResponse from(TradeEntity trade) {
        return new TradeResponse(
                trade.getId(),
                trade.getChatRoomId(),
                trade.getRequestPostId(),
                trade.getTalentPostId(),
                trade.getPayerId(),
                trade.getPayeeId(),
                trade.getAmount(),
                trade.getStatus(),
                trade.getPaidAt(),
                trade.getCompletedAt(),
                trade.getCancelledAt(),
                trade.getCreatedAt()
        );
    }
}
