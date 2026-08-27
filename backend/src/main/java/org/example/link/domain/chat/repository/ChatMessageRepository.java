package org.example.link.domain.chat.repository;

import org.example.link.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @EntityGraph(attributePaths = {"sender", "trade"})
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    void deleteByChatRoomId(Long chatRoomId);
    boolean existsByChatRoomId(Long chatRoomId);

    @Query("select m.attachmentPath from ChatMessage m "
            + "where m.chatRoom.id = :chatRoomId and m.attachmentPath is not null")
    List<String> findAttachmentPathsByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
