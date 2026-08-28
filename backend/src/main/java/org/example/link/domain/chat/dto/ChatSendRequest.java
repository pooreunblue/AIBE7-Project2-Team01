package org.example.link.domain.chat.dto;

import java.util.UUID;

import org.example.link.domain.chat.entity.ChatMessage;

public record ChatSendRequest(
        UUID chatRoomId,
        String content,
        ChatMessage.MessageType messageType
) {
}
