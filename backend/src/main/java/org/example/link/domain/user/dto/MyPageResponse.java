package org.example.link.domain.user.dto;

import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.wallet.entity.WalletEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record MyPageResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        BigDecimal walletBalance,
        Instant createdAt
) {
    public static MyPageResponse from(
            UserEntity user,
            WalletEntity wallet
    ) {
        return new MyPageResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                wallet.getBalance(),
                user.getCreatedAt()
        );
    }
}
