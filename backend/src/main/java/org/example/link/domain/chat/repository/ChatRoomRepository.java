package org.example.link.domain.chat.repository;

import java.util.UUID;

import org.example.link.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Query("""
            select cr from ChatRoom cr
            where ((:requestPostId is not null and cr.requestPostId = :requestPostId)
                or (:talentPostId is not null and cr.talentPostId = :talentPostId))
            and exists (select 1 from ChatParticipant p where p.chatRoom = cr and p.user.id = :userAId)
            and exists (select 1 from ChatParticipant p where p.chatRoom = cr and p.user.id = :userBId)
            """)
    Optional<ChatRoom> findExistingRoom(
            @Param("requestPostId") UUID requestPostId,
            @Param("talentPostId") UUID talentPostId,
            @Param("userAId") UUID userAId,
            @Param("userBId") UUID userBId
    );
}
