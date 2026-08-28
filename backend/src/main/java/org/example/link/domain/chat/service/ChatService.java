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
import org.example.link.domain.trade.repository.TradeRepository;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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

    @Transactional
    public ChatMessageResponse sendMessage(String senderEmail, ChatSendRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.chatRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        UserEntity sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        requireParticipant(chatRoom.getId(), sender.getId());

        ChatMessage message = new ChatMessage(
                chatRoom,
                sender,
                request.content(),
                request.messageType()
        );
        ChatMessage savedMessage = chatMessageRepository.save(message);
        return ChatMessageResponse.from(savedMessage);
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
            return ChatRoomResponse.from(existingRoom, other);
        }

        ChatRoom chatRoom = new ChatRoom(request.requestPostId(), request.talentPostId());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        chatParticipantRepository.save(new ChatParticipant(savedRoom, creator));
        chatParticipantRepository.save(new ChatParticipant(savedRoom, other));

        return ChatRoomResponse.from(savedRoom, other);
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

        List<ChatParticipant> myParticipations = chatParticipantRepository.findByUserId(user.getId());
        List<UUID> roomIds = myParticipations.stream()
                .map(p -> p.getChatRoom().getId())
                .toList();

        Map<UUID, UserEntity> otherUserByRoomId = chatParticipantRepository
                .findByChatRoomIdInAndUserIdNot(roomIds, user.getId()).stream()
                .collect(Collectors.toMap(p -> p.getChatRoom().getId(), ChatParticipant::getUser));

        return myParticipations.stream()
                .map(ChatParticipant::getChatRoom)
                .map(room -> ChatRoomResponse.from(room, otherUserByRoomId.get(room.getId())))
                .toList();
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
