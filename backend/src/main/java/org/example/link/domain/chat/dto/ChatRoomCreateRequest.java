package org.example.link.domain.chat.dto;

public record ChatRoomCreateRequest(
        Long requestPostId,
        Long talentPostId,
        Long otherUserId
) {
}
