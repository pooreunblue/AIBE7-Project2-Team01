package org.example.link.domain.chat.repository;

import org.example.link.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            select cr from ChatRoom cr
            where ((:requestPostId is not null and cr.requestPostId = :requestPostId)
                or (:talentPostId is not null and cr.talentPostId = :talentPostId))
            and exists (select 1 from ChatParticipant p where p.chatRoom = cr and p.user.id = :userAId)
            and exists (select 1 from ChatParticipant p where p.chatRoom = cr and p.user.id = :userBId)
            """)
    Optional<ChatRoom> findExistingRoom(
            @Param("requestPostId") Long requestPostId,
            @Param("talentPostId") Long talentPostId,
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId
    );
}
