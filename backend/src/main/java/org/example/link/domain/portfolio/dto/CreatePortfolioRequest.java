package org.example.link.domain.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePortfolioRequest (
        @NotBlank
        @Size(max = 100)
        String title,
        @NotBlank
        String description
){
}
