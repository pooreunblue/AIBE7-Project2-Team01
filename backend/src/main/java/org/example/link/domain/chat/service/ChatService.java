package org.example.link.domain.chat.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.service.StorageService;
import org.example.link.common.storage.type.FileType;
import org.example.link.domain.chat.dto.ChatMessageResponse;
import org.example.link.domain.chat.dto.ChatRoomCreateRequest;
import org.example.link.domain.chat.dto.ChatRoomResponse;
import org.example.link.domain.chat.dto.ChatSendRequest;
import org.example.link.domain.chat.entity.ChatMessage;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;
    private final StorageService storageService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final RequestPostRepository requestPostRepository;
    private final TalentPostRepository talentPostRepository;

    @Transactional
    public ChatMessageResponse sendMessage(String senderEmail, ChatSendRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.chatRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        UserEntity sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        requireParticipant(chatRoom.getId(), sender.getId());
        if (request.messageType() != ChatMessage.MessageType.TEXT) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        ChatMessage message = new ChatMessage(
                chatRoom,
                sender,
                request.content(),
                request.messageType()
        );
        ChatMessage savedMessage = chatMessageRepository.save(message);
        return ChatMessageResponse.from(savedMessage);
    }

    @Transactional
    public ChatMessageResponse requestTradeAmount(UUID userId, UUID chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        requireParticipant(chatRoomId, userId);

        if (chatRoom.getRequestPostId() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (tradeRepository.existsByChatRoomIdAndStatusIn(
                chatRoomId, List.of(org.example.link.domain.trade.entity.TradeStatus.PENDING,
                        org.example.link.domain.trade.entity.TradeStatus.PAID))) {
            throw new CustomException(ErrorCode.TRADE_ALREADY_IN_PROGRESS);
        }

        var requestPost = requestPostRepository.findById(chatRoom.getRequestPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!requestPost.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return chatMessagePublisher.publishTradeAmountRequest(
                chatRoom, sender, "거래 금액 설정을 요청했습니다."
        );
    }

    // 이미지 바이너리는 REST 멀티파트로 받아 Supabase 버킷에 저장하고,
    // 생성된 메시지는 텍스트와 동일하게 /topic/chat-rooms/{id} 로 브로드캐스트한다.
    @Transactional
    public ChatMessageResponse sendImage(String senderEmail, UUID chatRoomId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE);
        }
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        UserEntity sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        requireParticipant(chatRoomId, sender.getId());

        StoredFile stored = storageService.upload(file, "chat/" + chatRoomId, FileType.IMAGE);
        return chatMessagePublisher.publishImage(chatRoom, sender, stored.url(), stored.path());
    }

    private void requireParticipant(UUID chatRoomId, UUID userId) {
        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    @Transactional
    public ChatRoomResponse createRoom(String creatorEmail, ChatRoomCreateRequest request) {
        boolean hasRequestPost = request.requestPostId() != null;
        boolean hasTalentPost = request.talentPostId() != null;
        if (hasRequestPost == hasTalentPost) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        UserEntity creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserEntity other = userRepository.findById(request.otherUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatRoom existingRoom = chatRoomRepository.findExistingRoom(
                request.requestPostId(), request.talentPostId(), creator.getId(), other.getId()
        ).orElse(null);
        if (existingRoom != null) {
            return ChatRoomResponse.from(existingRoom, other, resolvePostTitle(existingRoom));
        }

        ChatRoom chatRoom = new ChatRoom(request.requestPostId(), request.talentPostId());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        chatParticipantRepository.save(new ChatParticipant(savedRoom, creator));
        chatParticipantRepository.save(new ChatParticipant(savedRoom, other));

        return ChatRoomResponse.from(savedRoom, other, resolvePostTitle(savedRoom));
    }

    // 나가는 사람의 참가자 row만 지움 (메시지/방은 그대로 유지 — 상대방은 계속 볼 수 있어야 함).
    // 양쪽 다 나가서 참가자가 0명이 되면, 걸린 거래(trade)가 없을 때만 방+메시지까지 완전히 정리함.
    @Transactional
    public void leaveRoom(String email, UUID chatRoomId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        requireParticipant(chatRoomId, user.getId());
        chatParticipantRepository.deleteByChatRoomIdAndUserId(chatRoomId, user.getId());

        boolean noParticipantsLeft = chatParticipantRepository.countByChatRoomId(chatRoomId) == 0;
        if (noParticipantsLeft && !tradeRepository.existsByChatRoomId(chatRoomId)) {
            deleteAttachments(chatRoomId);
            chatMessageRepository.deleteByChatRoomId(chatRoomId);
            chatRoomRepository.deleteById(chatRoomId);
        }
    }

    // 방이 완전히 삭제될 때 버킷에 남는 orphan 이미지를 정리한다. 정리 실패가 방 삭제를 막지 않도록 best-effort.
    private void deleteAttachments(UUID chatRoomId) {
        for (String path : chatMessageRepository.findAttachmentPathsByChatRoomId(chatRoomId)) {
            try {
                storageService.delete(path);
            } catch (RuntimeException ignored) {
                // 로깅만 하고 진행 (스토리지 정리는 방 삭제 성공보다 우선순위 낮음)
            }
        }
    }

    public List<ChatRoomResponse> getMyRooms(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<ChatParticipant> myParticipations = chatParticipantRepository.findByUserIdWithRoom(user.getId());
        if (myParticipations.isEmpty()) {
            return List.of();
        }

        List<ChatRoom> rooms = myParticipations.stream()
                .map(ChatParticipant::getChatRoom)
                .toList();
        List<UUID> roomIds = rooms.stream()
                .map(ChatRoom::getId)
                .toList();

        Map<UUID, UserEntity> otherUserByRoomId = chatParticipantRepository
                .findOthersWithUserByChatRoomIdIn(roomIds, user.getId()).stream()
                .collect(Collectors.toMap(p -> p.getChatRoom().getId(), ChatParticipant::getUser));

        Map<UUID, String> postTitleByRoomId = resolvePostTitles(rooms);

        return rooms.stream()
                .map(room -> ChatRoomResponse.from(
                        room,
                        otherUserByRoomId.get(room.getId()),
                        postTitleByRoomId.get(room.getId())
                ))
                .toList();
    }

    /** 방 목록의 게시글 제목을 종류별 일괄 조회(findByIdIn)로 한 번에 해결한다 (방마다 개별 조회하던 N+1 제거). */
    private Map<UUID, String> resolvePostTitles(List<ChatRoom> rooms) {
        Set<UUID> talentPostIds = rooms.stream()
                .map(ChatRoom::getTalentPostId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> requestPostIds = rooms.stream()
                .map(ChatRoom::getRequestPostId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, String> talentTitles = talentPostIds.isEmpty() ? Map.of()
                : talentPostRepository.findByIdIn(talentPostIds).stream()
                        .collect(Collectors.toMap(TalentPostEntity::getId, TalentPostEntity::getTitle));
        Map<UUID, String> requestTitles = requestPostIds.isEmpty() ? Map.of()
                : requestPostRepository.findByIdIn(requestPostIds).stream()
                        .collect(Collectors.toMap(RequestPostEntity::getId, RequestPostEntity::getTitle));

        Map<UUID, String> titleByRoomId = new HashMap<>();
        for (ChatRoom room : rooms) {
            String title = null;
            if (room.getTalentPostId() != null) {
                title = talentTitles.get(room.getTalentPostId());
            } else if (room.getRequestPostId() != null) {
                title = requestTitles.get(room.getRequestPostId());
            }
            titleByRoomId.put(room.getId(), title);
        }
        return titleByRoomId;
    }

    private String resolvePostTitle(ChatRoom room) {
        if (room.getTalentPostId() != null) {
            return talentPostRepository.findById(room.getTalentPostId())
                    .map(post -> post.getTitle())
                    .orElse(null);
        }

        if (room.getRequestPostId() != null) {
            return requestPostRepository.findById(room.getRequestPostId())
                    .map(post -> post.getTitle())
                    .orElse(null);
        }

        return null;
    }

    public List<ChatMessageResponse> getMessages(String email, UUID chatRoomId, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        requireParticipant(chatRoomId, user.getId());

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable)
                .map(ChatMessageResponse::from)
                .toList();
    }
}
