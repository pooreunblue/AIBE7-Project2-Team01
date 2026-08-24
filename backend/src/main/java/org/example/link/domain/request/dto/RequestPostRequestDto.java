package org.example.link.domain.request.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        Long categoryId,
        @NotBlank Long budgetMin,
        @NotBlank Long budgetMax
) {
}
