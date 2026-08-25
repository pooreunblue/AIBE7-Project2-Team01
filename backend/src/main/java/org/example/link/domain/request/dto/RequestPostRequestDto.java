package org.example.link.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Long categoryId,
        @NotNull Long budgetMin,
        @NotNull Long budgetMax
) {
}
