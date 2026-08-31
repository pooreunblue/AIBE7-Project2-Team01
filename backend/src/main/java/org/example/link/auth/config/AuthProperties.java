package org.example.link.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        Jwt jwt,
        Cookie cookie,
        String frontendOrigin
) {
    public record Jwt(
            String secret,
            Duration accessTokenExpiry,
            Duration refreshTokenExpiry
    ) {
    }

    public record Cookie(
            boolean secure,
            String sameSite
    ) {
    }
}
