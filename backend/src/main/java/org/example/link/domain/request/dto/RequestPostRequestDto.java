package org.example.link.domain.request.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

public record RequestPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull UUID categoryId,
        @NotNull Long budgetMin,
        @NotNull Long budgetMax,
        LocalDate dueDate
) {
    @AssertTrue(message = "최소 예산은 최대 예산보다 클 수 없습니다.")
    public boolean isBudgetRangeValid() {
        return budgetMin != null && budgetMax != null && budgetMin <= budgetMax;
    }
}
