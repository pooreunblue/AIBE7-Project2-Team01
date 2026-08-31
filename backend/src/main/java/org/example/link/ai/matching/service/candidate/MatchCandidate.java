package org.example.link.ai.matching.service.candidate;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.domain.talent.util.DurationUnit;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 화면에 전달할 하나의 추천 후보다.
 * TALENT 전용 필드와 REQUEST 전용 필드는 서로 사용하지 않는 경우 null이 된다.
 */
public record MatchCandidate(
        EmbeddingTargetType targetType,
        UUID targetId,
        UUID userId,
        String authorNickname,
        String authorProfileImageUrl,
        UUID categoryId,
        String categoryName,
        String title,
        String content,
        String thumbnailUrl,
        Long price,
        Integer estimatedDuration,
        DurationUnit durationUnit,
        Long budgetMin,
        Long budgetMax,
        LocalDate dueDate,
        double semanticScore,
        Double amountScore,
        double matchScore,
        String recommendationReason
) {
    public MatchCandidate withRecommendationReason(String reason) {
        return new MatchCandidate(
                targetType,
                targetId,
                userId,
                authorNickname,
                authorProfileImageUrl,
                categoryId,
                categoryName,
                title,
                content,
                thumbnailUrl,
                price,
                estimatedDuration,
                durationUnit,
                budgetMin,
                budgetMax,
                dueDate,
                semanticScore,
                amountScore,
                matchScore,
                reason
        );
    }
}
