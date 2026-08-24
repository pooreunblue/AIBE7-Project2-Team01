package org.example.link.domain.user.dto;

public record SignupResponse(
        Long id,
        String email,
        String nickname
) {
}
