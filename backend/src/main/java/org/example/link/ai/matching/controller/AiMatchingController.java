package org.example.link.ai.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.ai.matching.dto.AiMatchResponse;
import org.example.link.ai.matching.dto.SearchAiMatchRequest;
import org.example.link.ai.matching.service.AiMatchingService;
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

    /**
     * 자연어 검색어와 정형 조건을 받아 재능글 또는 요청글 추천 결과를 반환한다.
     */
    @PostMapping
    @Operation(summary = "AI 기반 재능글/요청글 매칭")
    public ApiResponse<AiMatchResponse> match(@Valid @RequestBody SearchAiMatchRequest request) {
        return ApiResponse.ok(aiMatchingService.match(request));
    }
}
