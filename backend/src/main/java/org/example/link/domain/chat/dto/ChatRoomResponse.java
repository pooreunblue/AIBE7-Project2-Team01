package org.example.link.domain.chat.dto;

import org.example.link.domain.chat.entity.ChatRoom;

import java.time.Instant;

public record ChatRoomResponse(
        Long chatRoomId,
        Long requestPostId,
        Long talentPostId,
        Instant createdAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getRequestPostId(),
                chatRoom.getTalentPostId(),
                chatRoom.getCreatedAt()
        );
    }
}
