package org.example.link.domain.chat.dto;

import org.example.link.domain.chat.entity.ChatMessage;
import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageResponseTest {

    @Test
    void resolvesTradeCancelledActionType() {
        ChatRoom chatRoom = chatRoom();
        UserEntity sender = user();
        TradeEntity trade = trade();
        trade.cancel();
        ChatMessage message = new ChatMessage(
                chatRoom,
                sender,
                "거래가 취소되었습니다.",
                ChatMessage.MessageType.SYSTEM,
                trade
        );
        ReflectionTestUtils.setField(message, "id", UUID.randomUUID());

        ChatMessageResponse response = ChatMessageResponse.from(message);

        assertThat(response.actionType()).isEqualTo("TRADE_CANCELLED");
        assertThat(response.trade().status()).isEqualTo(trade.getStatus());
    }

    @Test
    void doesNotResolveTradeActionWhenSystemMessageHasNoTrade() {
        ChatMessage message = new ChatMessage(
                chatRoom(),
                user(),
                "거래 완료되었습니다.",
                ChatMessage.MessageType.SYSTEM
        );
        ReflectionTestUtils.setField(message, "id", UUID.randomUUID());

        ChatMessageResponse response = ChatMessageResponse.from(message);

        assertThat(response.actionType()).isNull();
        assertThat(response.trade()).isNull();
    }

    private ChatRoom chatRoom() {
        ChatRoom chatRoom = new ChatRoom(UUID.randomUUID(), null);
        ReflectionTestUtils.setField(chatRoom, "id", UUID.randomUUID());
        return chatRoom;
    }

    private UserEntity user() {
        UserEntity user = new UserEntity("chat@test.com", "password", "chat-user");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private TradeEntity trade() {
        TradeEntity trade = new TradeEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(250_000)
        );
        ReflectionTestUtils.setField(trade, "id", UUID.randomUUID());
        return trade;
    }
}
