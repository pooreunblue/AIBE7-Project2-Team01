package org.example.link.domain.wallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ChargeRequest(
        @NotNull
        @DecimalMin(value = "1000")
        BigDecimal amount
) {
}
