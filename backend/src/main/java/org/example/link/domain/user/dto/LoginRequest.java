package org.example.link.domain.user.dto;

public record LoginRequest(
        String loginId,
        String password
) {
}