package org.example.link.domain.chat.dto;

import org.example.link.domain.chat.entity.ChatMessage;

import java.time.Instant;

public record ChatMessageResponse(
        Long chatMessageId,
        Long chatRoomId,
        Long senderId,
        String senderNickname,
        String content,
        ChatMessage.MessageType messageType,
        Instant createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getMessageType(),
                message.getCreatedAt()
        );
    }
}
