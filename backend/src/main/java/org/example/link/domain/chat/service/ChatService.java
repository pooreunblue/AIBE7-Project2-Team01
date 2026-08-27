package org.example.link.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
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

    @Transactional
    public ChatMessageResponse sendMessage(String senderEmail, ChatSendRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.chatRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        UserEntity sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
    public void leaveRoom(String email, Long chatRoomId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(chatRoomId, user.getId())) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
        chatParticipantRepository.deleteByChatRoomIdAndUserId(chatRoomId, user.getId());

        boolean noParticipantsLeft = chatParticipantRepository.countByChatRoomId(chatRoomId) == 0;
        if (noParticipantsLeft && !tradeRepository.existsByChatRoomId(chatRoomId)) {
            chatMessageRepository.deleteByChatRoomId(chatRoomId);
            chatRoomRepository.deleteById(chatRoomId);
        }
    }

    public List<ChatRoomResponse> getMyRooms(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<ChatParticipant> myParticipations = chatParticipantRepository.findByUserId(user.getId());
        List<Long> roomIds = myParticipations.stream()
                .map(p -> p.getChatRoom().getId())
                .toList();

        Map<Long, UserEntity> otherUserByRoomId = chatParticipantRepository
                .findByChatRoomIdInAndUserIdNot(roomIds, user.getId()).stream()
                .collect(Collectors.toMap(p -> p.getChatRoom().getId(), ChatParticipant::getUser));

        return myParticipations.stream()
                .map(ChatParticipant::getChatRoom)
                .map(room -> ChatRoomResponse.from(room, otherUserByRoomId.get(room.getId())))
                .toList();
    }

    public List<ChatMessageResponse> getMessages(String email, Long chatRoomId, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(chatRoomId, user.getId())) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable)
                .map(ChatMessageResponse::from)
                .toList();
    }
}
