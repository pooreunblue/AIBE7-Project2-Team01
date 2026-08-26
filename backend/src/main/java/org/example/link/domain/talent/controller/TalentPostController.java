package org.example.link.domain.talent.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.talent.dto.TalentPostRequestDto;
import org.example.link.domain.talent.dto.TalentPostResponseDto;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.service.TalentPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/talents")
public class TalentPostController {
    private final TalentPostService talentPostService;

    @PostMapping
    @Operation(summary = "재능글 등록")
    public ResponseEntity<ApiResponse<TalentPostResponseDto>> create(
            @Valid @RequestBody TalentPostRequestDto talentPostRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        TalentPostEntity talentPostEntity = talentPostService.create(talentPostRequestDto, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(TalentPostResponseDto.toDto(talentPostEntity)));
    }
}
