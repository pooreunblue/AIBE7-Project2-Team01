package org.example.link.auth.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.config.AuthProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {
    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private final AuthProperties authProperties;

    public ResponseCookie createAccessTokenCookie(
            String token
    ) {
        return ResponseCookie.from(
                ACCESS_TOKEN_COOKIE,
                        token
                )
                .httpOnly(true)
                .secure(false) // 로컬 개발
                .sameSite("Lax")
                .path("/")
                .maxAge(authProperties
                        .jwt()
                        .accessTokenExpiry()
                )
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(
            String token
    ) {
        return ResponseCookie.from(
                REFRESH_TOKEN_COOKIE,
                        token
                )
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(authProperties
                        .jwt()
                        .refreshTokenExpiry()
                )
                .build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    public Optional<String> getCookieValue(
            HttpServletRequest request,
            String name
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
