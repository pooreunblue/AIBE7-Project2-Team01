package org.example.link.domain.talent.dto;

import lombok.Builder;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.util.DurationUnit;
import org.example.link.domain.talent.util.TalentPostStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TalentPostResponseDto(
        Long talentPostId,
        Long userId,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Long price,
        Integer estimatedDuration,
        DurationUnit durationUnit,
        Long portfolioId,
        TalentPostStatus status,
        BigDecimal aiConfidence,
        Instant createdAt,
        Instant updatedAt
) {
    public static TalentPostResponseDto toDto(TalentPostEntity talentPostEntity) {
        return TalentPostResponseDto.builder()
                .talentPostId(talentPostEntity.getId())
                .userId(talentPostEntity.getUser().getId())
                .categoryId(talentPostEntity.getCategory().getId())
                .categoryName(talentPostEntity.getCategory().getName())
                .title(talentPostEntity.getTitle())
                .content(talentPostEntity.getContent())
                .price(talentPostEntity.getPrice())
                .estimatedDuration(talentPostEntity.getEstimatedDuration())
                .durationUnit(talentPostEntity.getDurationUnit())
                .portfolioId(talentPostEntity.getPortfolio() == null ? null : talentPostEntity.getPortfolio().getId())
                .status(talentPostEntity.getStatus())
                .aiConfidence(talentPostEntity.getAiConfidence())
                .createdAt(talentPostEntity.getCreatedAt())
                .updatedAt(talentPostEntity.getUpdatedAt())
                .build();
    }
}
