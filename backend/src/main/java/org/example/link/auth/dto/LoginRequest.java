package org.example.link.auth.dto;

public record LoginRequest(
        String loginId,
        String password
) {
}