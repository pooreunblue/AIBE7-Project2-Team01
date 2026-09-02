package org.example.link.ai.matching.dto;

import jakarta.validation.constraints.NotBlank;

/** 사용자가 AI 검색창에 입력한 자연어 문장이다. */
public record AnalyzeAiMatchRequest(
        @NotBlank String query
) {
}
