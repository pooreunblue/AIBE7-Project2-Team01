package org.example.link.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String nickname
) {
}
