package org.example.link.ai.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.ai.matching.dto.AnalyzeAiMatchRequest;
import org.example.link.ai.matching.dto.AnalyzeAiMatchResponse;
import org.example.link.ai.matching.dto.AiMatchResponse;
import org.example.link.ai.matching.dto.SearchAiMatchRequest;
import org.example.link.ai.matching.service.AiMatchingService;
import org.example.link.ai.matching.service.analysis.AiMatchQueryAnalysisService;
import org.example.link.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/matches")
public class AiMatchingController {
    private final AiMatchingService aiMatchingService;
    private final AiMatchQueryAnalysisService queryAnalysisService;

    /**
     * AI 검색창의 자연어 문장을 읽고 targetType과 필터 조건을 자동으로 채울 수 있게 분석한다.
     */
    @PostMapping("/analyze")
    @Operation(summary = "AI 매칭 검색어 분석")
    public ApiResponse<AnalyzeAiMatchResponse> analyze(
            @Valid @RequestBody AnalyzeAiMatchRequest request
    ) {
        return ApiResponse.ok(queryAnalysisService.analyze(request.query()));
    }

    /**
     * 자연어 검색어와 정형 조건을 받아 재능글 또는 요청글 추천 결과를 반환한다.
     */
    @PostMapping
    @Operation(summary = "AI 기반 재능글/요청글 매칭")
    public ApiResponse<AiMatchResponse> match(@Valid @RequestBody SearchAiMatchRequest request) {
        return ApiResponse.ok(aiMatchingService.match(request));
    }
}
