package org.example.link.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import org.example.link.domain.request.entity.RequestPostEntity;

public record RequestPostRequestDto(
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank Long budget_max,
        @NotBlank Long budget_min
) {
    public RequestPostEntity toEntity() {
        return RequestPostEntity.builder()
                .title(title)
                .content(content)
                .budget_max(budget_max)
                .budget_min(budget_min)
                .build();
    }
}
