package org.example.link.domain.talent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.link.domain.talent.util.DurationUnit;

import java.time.LocalDate;

public record TalentPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Long categoryId,
        @NotNull Long price,
        @NotNull Integer estimatedDuration,
        @NotNull DurationUnit durationUnit,
        @NotNull Long portfolioId
) {
}
