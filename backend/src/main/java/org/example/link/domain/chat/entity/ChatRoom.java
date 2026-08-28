package org.example.link.domain.chat.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chat_room_id")
    private UUID id;

    @Column(name = "request_post_id")
    private UUID requestPostId;

    @Column(name = "talent_post_id")
    private UUID talentPostId;

    public ChatRoom(UUID requestPostId, UUID talentPostId) {
        this.requestPostId = requestPostId;
        this.talentPostId = talentPostId;
    }
}
