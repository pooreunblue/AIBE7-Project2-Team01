package org.example.link.domain.portfolio.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.portfolio.dto.CreatePortfolioRequest;
import org.example.link.domain.portfolio.dto.UpdatePortfolioRequest;
import org.example.link.domain.portfolio.dto.PortfolioResponse;
import org.example.link.domain.portfolio.service.PortfolioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("/portfolios")
    @Operation(summary = "포트폴리오 등록")
    public ApiResponse<PortfolioResponse> createPortfolio(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreatePortfolioRequest request
    ) {
        return ApiResponse.ok(
                portfolioService.createPortfolio(
                        user.getUserId(),
                        request
                )
        );
    }

    @GetMapping("/users/me/portfolios")
    @Operation(summary = "내 포트폴리오 목록")
    public ApiResponse<List<PortfolioResponse>> getMyPortfolios(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(
                portfolioService.getMyPortfolios(
                        user.getUserId()
                )
        );
    }

    @GetMapping("/users/{userId}/portfolios")
    @Operation(summary = "사용자 포트폴리오 목록")
    public ApiResponse<List<PortfolioResponse>> getUserPortfolios(
            @PathVariable UUID userId
    ) {
        return ApiResponse.ok(
                portfolioService.getUserPortfolios(userId)
        );
    }

    @GetMapping("/users/public/{userId}/portfolios")
    @Operation(summary = "공개 사용자 포트폴리오 목록")
    public ApiResponse<List<PortfolioResponse>> getPublicUserPortfolios(
            @PathVariable UUID userId
    ) {
        return ApiResponse.ok(
                portfolioService.getUserPortfolios(userId)
        );
    }

    @GetMapping("/portfolios/{portfolioId}")
    @Operation(summary = "포트폴리오 상세")
    public ApiResponse<PortfolioResponse> getPortfolio(
            @PathVariable UUID portfolioId
    ) {
        return ApiResponse.ok(
                portfolioService.getPortfolio(portfolioId)
        );
    }

    @PatchMapping("/portfolios/{portfolioId}")
    @Operation(summary = "포트폴리오 수정")
    public ApiResponse<PortfolioResponse> updatePortfolio(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody UpdatePortfolioRequest request
    ) {
        return ApiResponse.ok(
                portfolioService.updatePortfolio(
                        user.getUserId(),
                        portfolioId,
                        request
                )
        );
    }

    @DeleteMapping("/portfolios/{portfolioId}")
    @Operation(summary = "포트폴리오 삭제")
    public ApiResponse<Void> deletePortfolio(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID portfolioId
    ) {
        portfolioService.deletePortfolio(
                user.getUserId(),
                portfolioId
        );

        return ApiResponse.ok();
    }
}
