package org.example.link.domain.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.portfolio.dto.PortfolioFileResponse;
import org.example.link.domain.portfolio.service.PortfolioFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolios/{portfolioId}/files")
public class PortfolioFileController {

    private final PortfolioFileService portfolioFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "포트폴리오 파일 업로드")
    public ResponseEntity<ApiResponse<PortfolioFileResponse>> uploadFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId,
            @RequestPart("file") MultipartFile file
    ) {
        PortfolioFileResponse response =
                portfolioFileService.uploadFile(
                        user.getUserId(),
                        portfolioId,
                        file
                );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "포트폴리오 파일 목록 조회")
    public ApiResponse<List<PortfolioFileResponse>> getFiles(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId
    ) {
        return ApiResponse.ok(
                portfolioFileService.getFiles(
                        user.getUserId(),
                        portfolioId
                )
        );
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "포트폴리오 파일 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId,
            @PathVariable Long fileId
    ) {

        portfolioFileService.deleteFile(
                user.getUserId(),
                portfolioId,
                fileId
        );

        return ResponseEntity.ok(
                ApiResponse.ok(null)
        );
    }

    @PatchMapping(
            value = "/{fileId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "포트폴리오 파일 교체")
    public ResponseEntity<ApiResponse<PortfolioFileResponse>> updateFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId,
            @PathVariable Long fileId,
            @RequestPart("file") MultipartFile file
    ) {

        PortfolioFileResponse response =
                portfolioFileService.updateFile(
                        user.getUserId(),
                        portfolioId,
                        fileId,
                        file
                );

        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }

    @PatchMapping("/{fileId}/thumbnail")
    @Operation(summary = "포트폴리오 대표 이미지 지정")
    public ApiResponse<PortfolioFileResponse> changeThumbnail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId,
            @PathVariable Long fileId
    ) {
        return ApiResponse.ok(
                portfolioFileService.changeThumbnail(
                        user.getUserId(),
                        portfolioId,
                        fileId
                )
        );
    }
}
