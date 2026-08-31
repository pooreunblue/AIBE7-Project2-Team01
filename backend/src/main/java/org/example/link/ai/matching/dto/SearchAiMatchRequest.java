package org.example.link.ai.matching.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.link.ai.embedding.enums.EmbeddingTargetType;

/**
 * 매칭 검색 요청이다.
 * query는 의미 검색에 사용하고, condition은 가격이나 카테고리 같은 정확한 조건에 사용한다.
 */
public record SearchAiMatchRequest(
        @NotBlank String query,
        @NotNull EmbeddingTargetType targetType,
        @Valid MatchCondition condition,
        @Min(1) @Max(5) Integer limit
) {
    /** 조건이 생략되면 모든 정형 조건을 비워서 검색한다. */
    public MatchCondition resolvedCondition() {
        if (condition == null) {
            return MatchCondition.empty();
        }
        return condition;
    }

    /** 결과 개수가 생략되면 기본값으로 5개를 반환한다. */
    public int resolvedLimit() {
        if (limit == null) {
            return 5;
        }
        return limit;
    }
}
