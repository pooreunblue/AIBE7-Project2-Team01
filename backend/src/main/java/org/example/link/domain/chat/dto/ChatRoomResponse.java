package org.example.link.domain.chat.dto;

import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.user.entity.UserEntity;

import java.time.Instant;

public record ChatRoomResponse(
        Long chatRoomId,
        Long requestPostId,
        Long talentPostId,
        Long otherUserId,
        String otherUserNickname,
        Instant createdAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom, UserEntity otherUser) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getRequestPostId(),
                chatRoom.getTalentPostId(),
                otherUser.getId(),
                otherUser.getNickname(),
                chatRoom.getCreatedAt()
        );
    }
}
