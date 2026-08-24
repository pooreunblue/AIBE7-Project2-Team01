package org.example.link.domain.wallet.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.domain.trade.entity.TradeEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wallet_transactions")
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private TradeEntity trade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private WalletTransactionEntity(
            WalletEntity wallet,
            TradeEntity trade,
            WalletTransactionType transactionType,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description
    ) {
        this.wallet = wallet;
        this.trade = trade;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public static WalletTransactionEntity createCharge(
            WalletEntity wallet,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {
        return new WalletTransactionEntity(
                wallet,
                null,
                WalletTransactionType.CHARGE,
                amount,
                balanceAfter,
                "지갑 충전"
        );
    }
}