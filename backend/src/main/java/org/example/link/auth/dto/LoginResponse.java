package org.example.link.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}