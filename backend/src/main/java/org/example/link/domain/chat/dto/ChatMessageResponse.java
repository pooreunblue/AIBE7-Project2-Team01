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
        String actionType,
        TradeInfo trade
) {
    private static final String TRADE_AMOUNT_REQUEST_CONTENT = "거래 금액 설정을 요청했습니다.";
    private static final String TRADE_PAID_CONTENT = "결제가 완료되었습니다.";
    private static final String TRADE_COMPLETED_CONTENT = "거래 완료되었습니다.";
    private static final String TRADE_CANCELLED_CONTENT = "거래가 취소되었습니다.";

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
                resolveActionType(message),
                message.getTrade() != null ? TradeInfo.from(message.getTrade()) : null
        );
    }

    private static String resolveActionType(ChatMessage message) {
        if (message.getMessageType() != ChatMessage.MessageType.SYSTEM) {
            return null;
        }
        if (TRADE_AMOUNT_REQUEST_CONTENT.equals(message.getContent())) {
            return "TRADE_AMOUNT_REQUEST";
        }
        if (TRADE_PAID_CONTENT.equals(message.getContent()) && message.getTrade() != null) {
            return "TRADE_PAID";
        }
        if (TRADE_COMPLETED_CONTENT.equals(message.getContent()) && message.getTrade() != null) {
            return "TRADE_COMPLETED";
        }
        if (TRADE_CANCELLED_CONTENT.equals(message.getContent()) && message.getTrade() != null) {
            return "TRADE_CANCELLED";
        }
        return null;
    }
}
