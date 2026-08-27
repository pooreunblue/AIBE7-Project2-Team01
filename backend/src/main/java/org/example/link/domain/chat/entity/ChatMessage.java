package org.example.link.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.user.entity.UserEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    // 거래 요청(TRADE_REQUEST) 메시지에만 연결됨. 그 외 타입은 null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private TradeEntity trade;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public ChatMessage(ChatRoom chatRoom, UserEntity sender, String content, MessageType messageType) {
        this(chatRoom, sender, content, messageType, null);
    }

    public ChatMessage(ChatRoom chatRoom, UserEntity sender, String content, MessageType messageType, TradeEntity trade) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType;
        this.trade = trade;
    }

    public enum MessageType {
        TEXT, IMAGE, SYSTEM, TRADE_REQUEST
    }
}
