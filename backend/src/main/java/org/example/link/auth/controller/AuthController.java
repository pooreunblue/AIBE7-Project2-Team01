package org.example.link.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.dto.LoginRequest;
import org.example.link.auth.dto.LoginResponse;
import org.example.link.auth.dto.RefreshRequest;
import org.example.link.auth.dto.RefreshResponse;
import org.example.link.auth.service.AuthService;
import org.example.link.common.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.ok(
                authService.login(request)
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            Authentication authentication
    ){
        String email = authentication.getName();
        authService.logout(email);
        return ApiResponse.ok(null);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ){
        return ApiResponse.ok(
                authService.refresh(request)
        );
    }
}
