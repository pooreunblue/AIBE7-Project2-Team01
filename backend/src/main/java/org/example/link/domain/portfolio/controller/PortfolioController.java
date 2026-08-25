package org.example.link.domain.portfolio.controller;

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
    public ApiResponse<List<PortfolioResponse>> getUserPortfolios(
            @PathVariable Long userId
    ) {
        return ApiResponse.ok(
                portfolioService.getUserPortfolios(userId)
        );
    }

    @GetMapping("/portfolios/{portfolioId}")
    public ApiResponse<PortfolioResponse> getPortfolio(
            @PathVariable Long portfolioId
    ) {
        return ApiResponse.ok(
                portfolioService.getPortfolio(portfolioId)
        );
    }

    @PatchMapping("/portfolios/{portfolioId}")
    public ApiResponse<PortfolioResponse> updatePortfolio(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId,
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
    public ApiResponse<Void> deletePortfolio(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long portfolioId
    ) {
        portfolioService.deletePortfolio(
                user.getUserId(),
                portfolioId
        );

        return ApiResponse.ok();
    }
}