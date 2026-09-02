package org.example.link.domain.chat.repository;

import java.util.Collection;
import java.util.UUID;

import org.example.link.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    /** 내가 참여한 방 목록. 방(chatRoom)까지 fetch join 해서 목록 조회 시 방마다 재조회(N+1)를 막는다. */
    @Query("select p from ChatParticipant p join fetch p.chatRoom where p.user.id = :userId")
    List<ChatParticipant> findByUserIdWithRoom(@Param("userId") UUID userId);

    /**
     * 주어진 방들에서 "내가 아닌" 참여자. 상대방(user)까지 fetch join 해서
     * 응답 변환 시 사용자마다 재조회(N+1)를 막는다.
     */
    @Query("""
            select p from ChatParticipant p
            join fetch p.user
            join fetch p.chatRoom
            where p.chatRoom.id in :chatRoomIds and p.user.id <> :userId
            """)
    List<ChatParticipant> findOthersWithUserByChatRoomIdIn(
            @Param("chatRoomIds") Collection<UUID> chatRoomIds,
            @Param("userId") UUID userId
    );

    boolean existsByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);

    List<ChatParticipant> findByChatRoomId(UUID chatRoomId);

    void deleteByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);

    long countByChatRoomId(UUID chatRoomId);
}
