package org.example.link.domain.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.domain.wallet.dto.ChargeRequest;
import org.example.link.domain.wallet.dto.WalletResponse;
import org.example.link.domain.wallet.service.WalletService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping
    public WalletResponse getWallet(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return walletService.getWallet(user.getUserId());
    }

    @PostMapping("/charge")
    public WalletResponse charge(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChargeRequest request
    ){
        return walletService.charge(
                user.getUserId(),
                request.amount()
        );
    }
}
