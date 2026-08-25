package org.example.link.domain.chat.repository;

import org.example.link.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByUserId(Long userId);
    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    List<ChatParticipant> findByChatRoomIdInAndUserIdNot(List<Long> chatRoomIds, Long userId);
}
