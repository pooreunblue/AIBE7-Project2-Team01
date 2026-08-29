package org.example.link.auth.cookie;

import org.example.link.auth.config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CookieUtilTest {

    private CookieUtil cookieUtil;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties(
                new AuthProperties.Jwt(
                        "test-secret",
                        Duration.ofMinutes(15),
                        Duration.ofDays(14)
                ),
                new AuthProperties.Cookie(true, "Strict"),
                "http://localhost:3000"
        );
        cookieUtil = new CookieUtil(properties);
    }

    @Test
    void accessTokenCookieUsesConfiguredSecurityAttributes() {
        ResponseCookie cookie = cookieUtil.createAccessTokenCookie("access-token");

        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void deletedCookieKeepsSecurityAttributesAndExpiresImmediately() {
        ResponseCookie cookie = cookieUtil.deleteRefreshTokenCookie();

        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
    }
}
