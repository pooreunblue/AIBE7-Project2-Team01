package org.example.link.domain.category.dto;

import lombok.Builder;
import org.example.link.domain.category.entity.CategoryEntity;

@Builder
public record CategoryResponseDto(
        Long id,
        String name
) {
    public static CategoryResponseDto toDto(CategoryEntity categoryEntity) {
        return CategoryResponseDto.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .build();
    }
}
