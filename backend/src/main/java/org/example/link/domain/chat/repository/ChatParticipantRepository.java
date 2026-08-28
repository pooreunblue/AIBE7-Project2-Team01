package org.example.link.domain.chat.repository;

import java.util.UUID;

import org.example.link.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {
    List<ChatParticipant> findByUserId(UUID userId);
    boolean existsByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);
    List<ChatParticipant> findByChatRoomIdInAndUserIdNot(List<UUID> chatRoomIds, UUID userId);
    List<ChatParticipant> findByChatRoomId(UUID chatRoomId);
    void deleteByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);
    long countByChatRoomId(UUID chatRoomId);
}
