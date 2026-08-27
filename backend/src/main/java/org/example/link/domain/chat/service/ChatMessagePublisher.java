package org.example.link.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.chat.dto.ChatMessageResponse;
import org.example.link.domain.chat.entity.ChatMessage;
import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.chat.repository.ChatMessageRepository;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.user.entity.UserEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 다른 도메인(예: 거래)에서 채팅 메시지를 저장하고 실시간으로 브로드캐스트할 때 사용한다.
 * ChatService / TradeService 간 순환참조를 피하기 위해 별도 컴포넌트로 분리했다.
 * 호출자의 트랜잭션 안에서 실행되는 것을 전제로 한다.
 */
@Component
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private static final String TOPIC_PREFIX = "/topic/chat-rooms/";

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageResponse publishTradeRequest(ChatRoom chatRoom, UserEntity sender, String content, TradeEntity trade) {
        ChatMessage saved = chatMessageRepository.save(
                new ChatMessage(chatRoom, sender, content, ChatMessage.MessageType.TRADE_REQUEST, trade)
        );
        ChatMessageResponse response = ChatMessageResponse.from(saved);
        messagingTemplate.convertAndSend(TOPIC_PREFIX + chatRoom.getId(), response);
        return response;
    }
}
