package org.example.link.auth.controller;

import com.nimbusds.oauth2.sdk.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.config.AuthProperties;
import org.example.link.auth.cookie.CookieUtil;
import org.example.link.auth.dto.LoginRequest;
import org.example.link.auth.dto.LoginResponse;
import org.example.link.auth.dto.RefreshRequest;
import org.example.link.auth.dto.RefreshResponse;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.auth.service.AuthService;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthProperties authProperties;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse tokens = authService.login(request);

        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(tokens.accessToken());
        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessCookie.toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(ApiResponse.ok(null));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        authService.logout(userDetails.getUsername());
        ResponseCookie accessCookie = cookieUtil.deleteAccessTokenCookie();
        ResponseCookie refreshCookie = cookieUtil.deleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessCookie.toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(ApiResponse.ok(null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request
    ) {

        String refreshToken =
                cookieUtil.getCookieValue(
                        request,
                        CookieUtil.REFRESH_TOKEN_COOKIE
                ).orElseThrow(() ->
                        new CustomException(
                                ErrorCode.INVALID_REFRESH_TOKEN
                        )
                );

        RefreshResponse response = authService.refresh(refreshToken);
        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(response.accessToken());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessCookie.toString()
                )
                .body(ApiResponse.ok(null));
    }
}
