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
        return createAuthCookie(
                ACCESS_TOKEN_COOKIE,
                token,
                authProperties.jwt().accessTokenExpiry()
        );
    }

    public ResponseCookie createRefreshTokenCookie(
            String token
    ) {
        return createAuthCookie(
                REFRESH_TOKEN_COOKIE,
                token,
                authProperties.jwt().refreshTokenExpiry()
        );
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return deleteAuthCookie(ACCESS_TOKEN_COOKIE);
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return deleteAuthCookie(REFRESH_TOKEN_COOKIE);
    }

    private ResponseCookie createAuthCookie(
            String name,
            String value,
            Duration maxAge
    ) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(authProperties.cookie().secure())
                .sameSite(authProperties.cookie().sameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie deleteAuthCookie(String name) {
        return createAuthCookie(name, "", Duration.ZERO);
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
