package org.example.link.ai.matching.dto;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;

/** 자연어 검색어에서 추출한 검색 대상과 정형 조건이다. */
public record AnalyzeAiMatchResponse(
        String originalQuery,
        String semanticQuery,
        EmbeddingTargetType targetType,
        MatchCondition condition,
        String categoryName
) {
}
