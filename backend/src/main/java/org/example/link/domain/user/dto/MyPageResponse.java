package org.example.link.domain.user.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MyPageResponse(
        Long userId,
        String email,
        String nickname,
//        String profileImageUrl,
        BigDecimal walletBalance,
        Instant createdAt
) {
}
