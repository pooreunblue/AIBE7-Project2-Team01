package org.example.link.domain.portfolio.controller;

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

@RestController

@RequiredArgsConstructor

@RequestMapping("/portfolios/{portfolioId}/files")

public class PortfolioFileController {

    private final PortfolioFileService portfolioFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @DeleteMapping("/{fileId}")
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
    public ResponseEntity<ApiResponse<PortfolioFileResponse>> updateFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId,
            @PathVariable Long fileId,
            @RequestPart MultipartFile file
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
}
