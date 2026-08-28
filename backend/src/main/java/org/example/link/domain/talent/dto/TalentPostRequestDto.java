package org.example.link.domain.talent.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.link.domain.talent.util.DurationUnit;

public record TalentPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull UUID categoryId,
        @NotNull Long price,
        @NotNull Integer estimatedDuration,
        @NotNull DurationUnit durationUnit,
        UUID portfolioId
) {
}
