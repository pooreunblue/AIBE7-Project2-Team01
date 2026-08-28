package org.example.link.domain.chat.repository;

import java.util.UUID;

import org.example.link.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    @EntityGraph(attributePaths = {"sender", "trade"})
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(UUID chatRoomId, Pageable pageable);
    void deleteByChatRoomId(UUID chatRoomId);
    boolean existsByChatRoomId(UUID chatRoomId);

    @Query("select m.attachmentPath from ChatMessage m "
            + "where m.chatRoom.id = :chatRoomId and m.attachmentPath is not null")
    List<String> findAttachmentPathsByChatRoomId(@Param("chatRoomId") UUID chatRoomId);
}
