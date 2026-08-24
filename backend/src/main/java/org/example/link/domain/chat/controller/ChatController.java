package org.example.link.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.chat.dto.ChatMessageResponse;
import org.example.link.domain.chat.dto.ChatSendRequest;
import org.example.link.domain.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatSendRequest request, Principal principal) {
        ChatMessageResponse response = chatService.sendMessage(principal.getName(), request);
        messagingTemplate.convertAndSend(
                "/topic/chat-rooms/" + request.chatRoomId(),
                response
        );
    }
}
