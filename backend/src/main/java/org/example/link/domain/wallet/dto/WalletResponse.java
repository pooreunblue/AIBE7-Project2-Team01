package org.example.link.domain.wallet.dto;

import java.util.UUID;

import org.example.link.domain.wallet.entity.WalletEntity;

import java.math.BigDecimal;

public record WalletResponse(
        UUID walletId,
        BigDecimal balance
) {
    public static WalletResponse from(WalletEntity wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance()
        );
    }
}
