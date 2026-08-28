package org.example.link.domain.wallet.dto;

import java.util.UUID;

import org.example.link.domain.wallet.entity.WalletTransactionEntity;
import org.example.link.domain.wallet.entity.WalletTransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransactionResponse(
        UUID transactionId,
        WalletTransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {
    public static WalletTransactionResponse from(
            WalletTransactionEntity transaction
    ) {
        return new WalletTransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
