package org.example.link.domain.category.dto;

import java.util.UUID;

import lombok.Builder;
import org.example.link.domain.category.entity.CategoryEntity;

@Builder
public record CategoryResponseDto(
        UUID categoryId,
        String name
) {
    public static CategoryResponseDto toDto(CategoryEntity categoryEntity) {
        return CategoryResponseDto.builder()
                .categoryId(categoryEntity.getId())
                .name(categoryEntity.getName())
                .build();
    }
}
