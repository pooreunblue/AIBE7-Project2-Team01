package org.example.link.domain.user.dto;

public record SignupRequest(
        String loginId,
        String password,
        String nickname
) {
}
