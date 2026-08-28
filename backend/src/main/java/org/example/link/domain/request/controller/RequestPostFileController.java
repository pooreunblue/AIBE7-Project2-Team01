package org.example.link.domain.request.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.request.dto.RequestPostFileResponse;
import org.example.link.domain.request.service.RequestPostFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests/{requestPostId}/files")
public class RequestPostFileController {
    private final RequestPostFileService requestPostFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "의뢰글 파일 업로드")
    public ResponseEntity<ApiResponse<RequestPostFileResponse>> uploadFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID requestPostId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                requestPostFileService.uploadFile(user, requestPostId, file)));
    }

    @GetMapping
    @Operation(summary = "의뢰글 파일 목록 조회")
    public ApiResponse<List<RequestPostFileResponse>> getFiles(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID requestPostId) {
        return ApiResponse.ok(requestPostFileService.getFiles(user, requestPostId));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "의뢰글 파일 삭제")
    public ApiResponse<Void> deleteFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID requestPostId,
            @PathVariable UUID fileId) {
        requestPostFileService.deleteFile(user, requestPostId, fileId);
        return ApiResponse.ok();
    }

    @PatchMapping(value = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "의뢰글 파일 교체")
    public ApiResponse<RequestPostFileResponse> updateFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID requestPostId,
            @PathVariable UUID fileId,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(requestPostFileService.updateFile(user, requestPostId, fileId, file));
    }

    @PatchMapping("/{fileId}/thumbnail")
    @Operation(summary = "의뢰글 대표 이미지 지정")
    public ApiResponse<RequestPostFileResponse> changeThumbnail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID requestPostId,
            @PathVariable UUID fileId) {
        return ApiResponse.ok(requestPostFileService.changeThumbnail(user, requestPostId, fileId));
    }
}
