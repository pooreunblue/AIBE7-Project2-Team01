package org.example.link.domain.talent.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.request.dto.RequestPostRequestDto;
import org.example.link.domain.request.dto.RequestPostResponseDto;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.talent.dto.TalentPostRequestDto;
import org.example.link.domain.talent.dto.TalentPostResponseDto;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.service.TalentPostService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

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

    @GetMapping
    @Operation(summary = "재능글 목록 조회")
    public ApiResponse<List<TalentPostResponseDto>> readAll() {
        List<TalentPostEntity> talentPostEntities = talentPostService.readAll();
        return ApiResponse.ok(talentPostEntities.stream()
                .map(TalentPostResponseDto::toDto)
                .toList());
    }

    @GetMapping("/{talentPostId}")
    @Operation(summary = "재능글 상세 조회")
    public ApiResponse<TalentPostResponseDto> readOne(@PathVariable Long talentPostId) {
        TalentPostEntity talentPostEntity = talentPostService.readOne(talentPostId);
        return ApiResponse.ok(TalentPostResponseDto.toDto(talentPostEntity));
    }

    @GetMapping("/search")
    @Operation(summary = "재능글 검색")
    public ApiResponse<Page<TalentPostResponseDto>> searchTalents(
            @RequestParam(required = false) String keyword,
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<TalentPostEntity> talentPostEntities = talentPostService.search(keyword, pageable);
        return ApiResponse.ok(talentPostEntities.map(TalentPostResponseDto::toDto));
    }

    @PutMapping("/{talentPostId}")
    @Operation(summary = "재능글 수정")
    public ApiResponse<TalentPostResponseDto> update(
            @PathVariable Long talentPostId,
            @Valid @RequestBody TalentPostRequestDto talentPostRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws AccessDeniedException {
        TalentPostEntity updated = talentPostService.update(talentPostId, talentPostRequestDto, userDetails);
        return ApiResponse.ok(TalentPostResponseDto.toDto(updated));
    }

    @DeleteMapping("/{talentPostId}")
    @Operation(summary = "재능글 삭제")
    public ApiResponse<Void> delete(
            @PathVariable Long talentPostId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws AccessDeniedException {
        talentPostService.delete(talentPostId, userDetails);
        return ApiResponse.ok();
    }

    @PatchMapping("/{talentPostId}/inactive")
    @Operation(summary = "재능글 비활성화")
    public ApiResponse<TalentPostResponseDto> inactive(
            @PathVariable Long talentPostId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws AccessDeniedException {
        TalentPostEntity inactived = talentPostService.inactiveStatus(talentPostId, userDetails);
        return ApiResponse.ok(TalentPostResponseDto.toDto(inactived));
    }
}
