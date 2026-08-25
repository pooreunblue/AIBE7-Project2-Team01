package org.example.link.domain.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.common.response.PageResponse;
import org.example.link.domain.wallet.dto.ChargeRequest;
import org.example.link.domain.wallet.dto.WalletResponse;
import org.example.link.domain.wallet.dto.WalletTransactionResponse;
import org.example.link.domain.wallet.service.TransactionService;
import org.example.link.domain.wallet.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final TransactionService transactionService;

    @GetMapping
    public ApiResponse<WalletResponse> getWallet(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(walletService.getWallet(user.getUserId()));
    }

    @PostMapping("/charge")
    public ApiResponse<WalletResponse> charge(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChargeRequest request
    ){
        return ApiResponse.ok(walletService.charge(
                user.getUserId(),
                request.amount()
        ));
    }

    @GetMapping("/transactions")
    public ApiResponse<PageResponse<WalletTransactionResponse>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable
    ) {
        Page<WalletTransactionResponse> page =
                transactionService.getTransactions(user.getUserId(), pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }
}
