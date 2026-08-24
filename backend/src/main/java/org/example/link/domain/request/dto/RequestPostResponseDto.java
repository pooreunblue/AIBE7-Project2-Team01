package org.example.link.domain.request.dto;

import lombok.Builder;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.util.RequestPostStatus;

import java.math.BigDecimal;

@Builder
public record RequestPostResponseDto(
        Long requestPostId,
        Long userId,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Long budgetMin,
        Long budgetMax,
        RequestPostStatus status,
        BigDecimal aiConfidence
) {
    public static RequestPostResponseDto toDto(RequestPostEntity requestPostEntity) {
        return RequestPostResponseDto.builder()
                .requestPostId(requestPostEntity.getId())
                .userId(requestPostEntity.getUser().getId())
                .categoryId(requestPostEntity.getCategory().getId())
                .categoryName(requestPostEntity.getCategory().getName())
                .title(requestPostEntity.getTitle())
                .content(requestPostEntity.getContent())
                .budgetMin(requestPostEntity.getBudgetMin())
                .budgetMax(requestPostEntity.getBudgetMax())
                .status(requestPostEntity.getStatus())
                .aiConfidence(requestPostEntity.getAiConfidence())
                .build();
    }
}
