package org.example.link.domain.chat.dto;

import java.util.UUID;

import org.example.link.domain.chat.entity.ChatMessage;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ChatMessageResponse(
        UUID chatMessageId,
        UUID chatRoomId,
        UUID senderId,
        String senderNickname,
        String content,
        ChatMessage.MessageType messageType,
        Instant createdAt,
        TradeInfo trade
) {
    // 거래 요청 카드 렌더링용. TRADE_REQUEST 메시지가 아니면 null.
    public record TradeInfo(
            UUID tradeId,
            BigDecimal amount,
            TradeStatus status,
            UUID payerId,
            UUID payeeId,
            String postType
    ) {
        public static TradeInfo from(TradeEntity trade) {
            String postType = trade.getRequestPostId() != null ? "REQUEST" : "TALENT";
            return new TradeInfo(
                    trade.getId(),
                    trade.getAmount(),
                    trade.getStatus(),
                    trade.getPayerId(),
                    trade.getPayeeId(),
                    postType
            );
        }
    }

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getMessageType(),
                message.getCreatedAt(),
                message.getTrade() != null ? TradeInfo.from(message.getTrade()) : null
        );
    }
}
