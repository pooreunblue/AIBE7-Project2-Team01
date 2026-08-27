package org.example.link.domain.trade.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trades")
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long id;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "request_post_id")
    private Long requestPostId;

    @Column(name = "talent_post_id")
    private Long talentPostId;

    @Column(name = "payer_id", nullable = false)
    private Long payerId;

    @Column(name = "payee_id", nullable = false)
    private Long payeeId;

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

    public TradeEntity(Long chatRoomId, Long requestPostId, Long talentPostId, Long payerId, Long payeeId, BigDecimal amount) {
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
        this.status = TradeStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void complete() {
        this.status = TradeStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        this.status = TradeStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }
}
