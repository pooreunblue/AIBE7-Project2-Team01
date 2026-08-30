package org.example.link.ai.generation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.ai.generation.dto.PostGenerationResponse;
import org.example.link.ai.generation.dto.RequestPostGenerationRequest;
import org.example.link.ai.generation.dto.TalentPostGenerationRequest;
import org.example.link.ai.generation.service.AiGenerationService;
import org.example.link.common.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/generation")
public class AiGenerationController {
    private final AiGenerationService aiGenerationService;

    @PostMapping(value = "/requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostGenerationResponse> generateRequest(
            @Valid @RequestPart("data") RequestPostGenerationRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.ok(aiGenerationService.generateRequest(request, image));
    }

    @PostMapping(value = "/talents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostGenerationResponse> generateTalent(
            @Valid @RequestPart("data") TalentPostGenerationRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.ok(aiGenerationService.generateTalent(request, image));
    }
}
