package org.example.link.domain.request.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RequestPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull UUID categoryId,
        @NotNull Long budgetMin,
        @NotNull Long budgetMax,
        LocalDate dueDate
) {
}
