package org.example.link.ai.generation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record RequestPostGenerationRequest(
        @NotBlank String content,
        @NotNull UUID categoryId,
        @NotNull Long budgetMin,
        @NotNull Long budgetMax,
        LocalDate dueDate
) {
}
