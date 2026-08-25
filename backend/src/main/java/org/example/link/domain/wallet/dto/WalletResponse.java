package org.example.link.domain.wallet.dto;

import org.example.link.domain.wallet.entity.WalletEntity;

import java.math.BigDecimal;

public record WalletResponse(
        Long walletId,
        BigDecimal balance
) {
    public static WalletResponse from(WalletEntity wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance()
        );
    }
}
