package org.example.link.domain.trade.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trades")
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "trade_id")
    private UUID id;

    @Column(name = "chat_room_id")
    private UUID chatRoomId;

    @Column(name = "request_post_id")
    private UUID requestPostId;

    @Column(name = "talent_post_id")
    private UUID talentPostId;

    @Column(name = "payer_id", nullable = false)
    private UUID payerId;

    @Column(name = "payee_id", nullable = false)
    private UUID payeeId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TradeEntity(UUID chatRoomId, UUID requestPostId, UUID talentPostId, UUID payerId, UUID payeeId, BigDecimal amount) {
        this.chatRoomId = chatRoomId;
        this.requestPostId = requestPostId;
        this.talentPostId = talentPostId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.status = TradeStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();

        if (this.status == null) {
            this.status = TradeStatus.PENDING;
        }
    }

    public void paid() {
        requireStatus(TradeStatus.PENDING);
        this.status = TradeStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void complete() {
        requireStatus(TradeStatus.PAID);
        this.status = TradeStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        requireStatus(TradeStatus.PENDING);
        this.status = TradeStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    private void requireStatus(TradeStatus expectedStatus) {
        if (this.status != expectedStatus) {
            throw new CustomException(ErrorCode.INVALID_TRADE_STATUS);
        }
    }
}
