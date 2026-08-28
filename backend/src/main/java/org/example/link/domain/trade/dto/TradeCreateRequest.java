package org.example.link.domain.trade.dto;

import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TradeCreateRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        UUID requestPostId,
        UUID talentPostId
) {
}
