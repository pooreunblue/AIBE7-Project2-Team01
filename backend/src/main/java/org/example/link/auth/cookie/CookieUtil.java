package org.example.link.auth.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil {

    public ResponseCookie createAccessTokenCookie(
            String token,
            Duration expiry
    ) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // 로컬 개발
                .sameSite("Lax")
                .path("/")
                .maxAge(expiry)
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(
            String token,
            Duration expiry
    ) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/auth")
                .maxAge(expiry)
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
                .path("/auth")
                .maxAge(0)
                .build();
    }
}
