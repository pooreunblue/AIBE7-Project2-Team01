package org.example.link.domain.user.dto;

import java.util.UUID;

public record SignupResponse(
        UUID id,
        String email,
        String nickname
) {
}
