package org.example.link.domain.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.request.dto.RequestPostRequestDto;
import org.example.link.domain.request.dto.RequestPostResponseDto;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.service.RequestPostService;
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
@RequestMapping("/requests")
public class RequestPostController {
    private final RequestPostService requestPostService;

    @PostMapping
    @Operation(summary = "의뢰글 등록")
    public ResponseEntity<ApiResponse<RequestPostResponseDto>> create(
            @Valid @RequestBody RequestPostRequestDto requestPostRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        RequestPostEntity requestPostEntity = requestPostService.create(requestPostRequestDto, userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(RequestPostResponseDto.toDto(requestPostEntity)));
    }

    @GetMapping
    @Operation(summary = "의뢰글 목록 조회")
    public ApiResponse<List<RequestPostResponseDto>> readAll() {
        List<RequestPostEntity> requestPostEntities = requestPostService.readAll();
        return ApiResponse.ok(requestPostEntities.stream()
                .map(RequestPostResponseDto::toDto)
                .toList());
    }

    @GetMapping("/{requestPostId}")
    @Operation(summary = "의뢰글 상세 조회")
    public ApiResponse<RequestPostResponseDto> readOne(@PathVariable Long requestPostId) {
        RequestPostEntity requestPostEntity = requestPostService.readOne(requestPostId);
        return ApiResponse.ok(RequestPostResponseDto.toDto(requestPostEntity));
    }

    @GetMapping("/search")
    @Operation(summary = "의뢰글 검색")
    public ApiResponse<Page<RequestPostResponseDto>> searchRequests(
            @RequestParam(required = false) String keyword,
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<RequestPostEntity> requestPostEntities = requestPostService.search(keyword, pageable);
        return ApiResponse.ok(requestPostEntities.map(RequestPostResponseDto::toDto));
    }

    @PutMapping("/{requestPostId}")
    @Operation(summary = "의뢰글 수정")
    public ApiResponse<RequestPostResponseDto> update(
            @PathVariable Long requestPostId,
            @Valid @RequestBody RequestPostRequestDto requestPostRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws AccessDeniedException {
        RequestPostEntity updated = requestPostService.update(requestPostId, requestPostRequestDto, userDetails);
        return ApiResponse.ok(RequestPostResponseDto.toDto(updated));
    }

    @DeleteMapping("/{requestPostId}")
    @Operation(summary = "의뢰글 삭제")
    public ApiResponse<Void> delete(
            @PathVariable Long requestPostId
    ) {
        requestPostService.delete(requestPostId);
        return ApiResponse.ok();
    }

    @PatchMapping("/{requestPostId}/close")
    @Operation(summary = "의뢰글 마감")
    public ApiResponse<RequestPostResponseDto> close(
            @PathVariable Long requestPostId
    ) {
        RequestPostEntity closed = requestPostService.closeStatus(requestPostId);
        return ApiResponse.ok(RequestPostResponseDto.toDto(closed));
    }
}
