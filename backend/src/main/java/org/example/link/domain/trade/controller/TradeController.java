package org.example.link.domain.trade.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.common.response.PageResponse;
import org.example.link.domain.trade.dto.TradeResponse;
import org.example.link.domain.trade.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trades")
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/{tradeId}")
    public ApiResponse<TradeResponse> getTrade(
            @PathVariable Long tradeId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(tradeService.getTrade(user.getUserId(), tradeId));
    }

    @GetMapping
    public ApiResponse<PageResponse<TradeResponse>> getMyTrades(
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable
    ) {
        Page<TradeResponse> page = tradeService.getMyTrades(user.getUserId(), pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }

    @PostMapping("/{tradeId}/pay")
    public ApiResponse<TradeResponse> pay(
            @PathVariable Long tradeId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(tradeService.pay(user.getUserId(), tradeId));
    }

    @PatchMapping("/{tradeId}/complete")
    public ApiResponse<TradeResponse> complete(
            @PathVariable Long tradeId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(tradeService.complete(user.getUserId(), tradeId));
    }

    @PatchMapping("/{tradeId}/cancel")
    public ApiResponse<TradeResponse> cancel(
            @PathVariable Long tradeId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(tradeService.cancel(user.getUserId(), tradeId));
    }
}
