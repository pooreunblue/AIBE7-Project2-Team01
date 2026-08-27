package org.example.link.domain.chat.dto;

import java.util.UUID;

import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.user.entity.UserEntity;

import java.time.Instant;

public record ChatRoomResponse(
        UUID chatRoomId,
        UUID requestPostId,
        UUID talentPostId,
        UUID otherUserId,
        String otherUserNickname,
        Instant createdAt
) {
    // otherUser가 null인 경우: 상대방이 채팅방을 나가서 참가자 row가 삭제된 상태. 방 자체는 남아있으니 목록엔 계속 보여주되, 나갔다는 걸 표시함.
    public static ChatRoomResponse from(ChatRoom chatRoom, UserEntity otherUser) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getRequestPostId(),
                chatRoom.getTalentPostId(),
                otherUser != null ? otherUser.getId() : null,
                otherUser != null ? otherUser.getNickname() : "(상대방이 나감)",
                chatRoom.getCreatedAt()
        );
    }
}
