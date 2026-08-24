package org.example.link.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.example.link.domain.request.entity.RequestPostEntity;

public record RequestPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        Long categoryId,
        @NotBlank Long budget_max,
        @NotBlank Long budget_min
) {
}
