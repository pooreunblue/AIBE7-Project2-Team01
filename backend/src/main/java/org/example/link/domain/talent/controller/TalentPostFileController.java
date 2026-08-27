package org.example.link.domain.talent.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.talent.dto.TalentPostFileResponse;
import org.example.link.domain.talent.service.TalentPostFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/talents/{talentPostId}/files")
public class TalentPostFileController {
    private final TalentPostFileService talentPostFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "재능글 파일 업로드")
    public ResponseEntity<ApiResponse<TalentPostFileResponse>> uploadFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long talentPostId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                talentPostFileService.uploadFile(user, talentPostId, file)));
    }

    @GetMapping
    @Operation(summary = "재능글 파일 목록 조회")
    public ApiResponse<List<TalentPostFileResponse>> getFiles(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long talentPostId) {
        return ApiResponse.ok(talentPostFileService.getFiles(user, talentPostId));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "재능글 파일 삭제")
    public ApiResponse<Void> deleteFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long talentPostId,
            @PathVariable Long fileId) {
        talentPostFileService.deleteFile(user, talentPostId, fileId);
        return ApiResponse.ok();
    }

    @PatchMapping(value = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "재능글 파일 교체")
    public ApiResponse<TalentPostFileResponse> updateFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long talentPostId,
            @PathVariable Long fileId,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(talentPostFileService.updateFile(user, talentPostId, fileId, file));
    }
}
