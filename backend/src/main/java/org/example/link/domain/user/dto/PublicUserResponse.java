package org.example.link.domain.user.dto;

import java.time.Instant;
import java.util.UUID;

import org.example.link.domain.user.entity.UserEntity;

public record PublicUserResponse(
        UUID userId,
        String nickname,
        String profileImageUrl,
        Instant createdAt
) {
    public static PublicUserResponse from(UserEntity user) {
        return new PublicUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }
}
