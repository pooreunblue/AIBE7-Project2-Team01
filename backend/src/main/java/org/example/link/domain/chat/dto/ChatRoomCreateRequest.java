package org.example.link.domain.chat.dto;

import java.util.UUID;

public record ChatRoomCreateRequest(
        UUID requestPostId,
        UUID talentPostId,
        UUID otherUserId
) {
}
