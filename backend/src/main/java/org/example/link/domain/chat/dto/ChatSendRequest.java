package org.example.link.domain.chat.dto;

import org.example.link.domain.chat.entity.ChatMessage;

public record ChatSendRequest(
        Long chatRoomId,
        String content,
        ChatMessage.MessageType messageType
) {
}
