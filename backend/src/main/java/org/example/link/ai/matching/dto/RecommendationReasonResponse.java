package org.example.link.ai.matching.dto;

import java.util.List;

/** Gemini가 반환하는 후보별 추천 이유 목록이다. */
public record RecommendationReasonResponse(
        List<RecommendationReasonItem> reasons
) {
    public record RecommendationReasonItem(
            String targetId,
            String reason
    ) {
    }
}
