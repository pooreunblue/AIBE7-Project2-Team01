package org.example.link.ai.matching.dto;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.service.candidate.MatchCandidate;

import java.util.List;

/** 매칭 검색어, 검색 대상, 점수순 후보 목록을 담는 최종 응답이다. */
public record AiMatchResponse(
        String query,
        EmbeddingTargetType targetType,
        List<MatchCandidate> candidates
) {
}
