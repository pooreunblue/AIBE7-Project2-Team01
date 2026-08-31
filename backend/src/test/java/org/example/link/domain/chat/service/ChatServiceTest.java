package org.example.link.domain.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.example.link.common.storage.service.StorageService;
import org.example.link.domain.chat.dto.ChatRoomResponse;
import org.example.link.domain.chat.entity.ChatParticipant;
import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.chat.repository.ChatMessageRepository;
import org.example.link.domain.chat.repository.ChatParticipantRepository;
import org.example.link.domain.chat.repository.ChatRoomRepository;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.example.link.domain.trade.repository.TradeRepository;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private final ChatParticipantRepository chatParticipantRepository = mock(ChatParticipantRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TalentPostRepository talentPostRepository = mock(TalentPostRepository.class);
    private final RequestPostRepository requestPostRepository = mock(RequestPostRepository.class);

    private final ChatService chatService = new ChatService(
            mock(ChatRoomRepository.class),
            mock(ChatMessageRepository.class),
            chatParticipantRepository,
            userRepository,
            mock(TradeRepository.class),
            mock(StorageService.class),
            mock(ChatMessagePublisher.class),
            requestPostRepository,
            talentPostRepository
    );

    @Test
    void getMyRoomsResolvesPostTitlesWithoutPerRoomQuery() {
        UserEntity me = user("나");
        UserEntity otherUserA = user("A");
        UserEntity otherUserB = user("B");
        UserEntity otherUserC = user("C");

        UUID talentId1 = UUID.randomUUID();
        UUID talentId2 = UUID.randomUUID();
        UUID requestId1 = UUID.randomUUID();

        ChatRoom room1 = room(talentId1, null);
        ChatRoom room2 = room(talentId2, null);
        ChatRoom room3 = room(null, requestId1);

        ChatParticipant myParticipant1 = participant(room1, me);
        ChatParticipant myParticipant2 = participant(room2, me);
        ChatParticipant myParticipant3 = participant(room3, me);
        ChatParticipant otherParticipant1 = participant(room1, otherUserA);
        ChatParticipant otherParticipant2 = participant(room2, otherUserB);
        ChatParticipant otherParticipant3 = participant(room3, otherUserC);

        TalentPostEntity talentPost1 = talentPost(talentId1, "재능글1");
        TalentPostEntity talentPost2 = talentPost(talentId2, "재능글2");
        RequestPostEntity requestPost1 = requestPost(requestId1, "요청글1");

        UUID myId = me.getId();
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(me));
        when(chatParticipantRepository.findByUserIdWithRoom(myId))
                .thenReturn(List.of(myParticipant1, myParticipant2, myParticipant3));
        when(chatParticipantRepository.findOthersWithUserByChatRoomIdIn(anyCollection(), eq(myId)))
                .thenReturn(List.of(otherParticipant1, otherParticipant2, otherParticipant3));
        when(talentPostRepository.findByIdIn(anyCollection()))
                .thenReturn(List.of(talentPost1, talentPost2));
        when(requestPostRepository.findByIdIn(anyCollection()))
                .thenReturn(List.of(requestPost1));

        List<ChatRoomResponse> result = chatService.getMyRooms("me@example.com");

        assertThat(result).hasSize(3);
        assertThat(result).extracting(ChatRoomResponse::postTitle)
                .containsExactly("재능글1", "재능글2", "요청글1");
        assertThat(result).extracting(ChatRoomResponse::otherUserNickname)
                .containsExactly("A", "B", "C");

        // N+1 제거 검증: 게시글 종류별로 딱 1회씩만, 방마다 개별 조회(findById) 없음
        verify(talentPostRepository, times(1)).findByIdIn(anyCollection());
        verify(requestPostRepository, times(1)).findByIdIn(anyCollection());
        verify(talentPostRepository, never()).findById(any());
        verify(requestPostRepository, never()).findById(any());
    }

    @Test
    void getMyRoomsReturnsEmptyWithoutQueryingPostRepositories() {
        UserEntity me = user("나");
        UUID myId = me.getId();
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(me));
        when(chatParticipantRepository.findByUserIdWithRoom(myId)).thenReturn(List.of());

        List<ChatRoomResponse> result = chatService.getMyRooms("me@example.com");

        assertThat(result).isEmpty();
        verify(chatParticipantRepository, never()).findOthersWithUserByChatRoomIdIn(anyCollection(), any());
        verify(talentPostRepository, never()).findByIdIn(anyCollection());
        verify(requestPostRepository, never()).findByIdIn(anyCollection());
    }

    private UserEntity user(String nickname) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getNickname()).thenReturn(nickname);
        when(user.getProfileImageUrl()).thenReturn(null);
        return user;
    }

    private ChatRoom room(UUID talentPostId, UUID requestPostId) {
        ChatRoom room = mock(ChatRoom.class);
        when(room.getId()).thenReturn(UUID.randomUUID());
        when(room.getTalentPostId()).thenReturn(talentPostId);
        when(room.getRequestPostId()).thenReturn(requestPostId);
        when(room.getCreatedAt()).thenReturn(Instant.now());
        return room;
    }

    private ChatParticipant participant(ChatRoom room, UserEntity user) {
        ChatParticipant participant = mock(ChatParticipant.class);
        when(participant.getChatRoom()).thenReturn(room);
        when(participant.getUser()).thenReturn(user);
        return participant;
    }

    private TalentPostEntity talentPost(UUID id, String title) {
        TalentPostEntity post = mock(TalentPostEntity.class);
        when(post.getId()).thenReturn(id);
        when(post.getTitle()).thenReturn(title);
        return post;
    }

    private RequestPostEntity requestPost(UUID id, String title) {
        RequestPostEntity post = mock(RequestPostEntity.class);
        when(post.getId()).thenReturn(id);
        when(post.getTitle()).thenReturn(title);
        return post;
    }
}
