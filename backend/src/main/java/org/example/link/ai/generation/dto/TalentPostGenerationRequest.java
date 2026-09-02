package org.example.link.ai.generation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.link.domain.talent.util.DurationUnit;

import java.util.UUID;

public record TalentPostGenerationRequest(
        @NotBlank String content,
        @NotNull UUID categoryId,
        @NotNull Long price,
        @NotNull Integer estimatedDuration,
        @NotNull DurationUnit durationUnit,
        UUID portfolioId
) {
}
