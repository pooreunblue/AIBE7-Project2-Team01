package org.example.link.domain.user.dto;

public record SignupResponse(
        Long id,
        String loginId,
        String nickname
) {
}
