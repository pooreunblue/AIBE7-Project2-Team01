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
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;

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

        ChatRoom chatRoom = new ChatRoom(request.requestPostId(), request.talentPostId());
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        chatParticipantRepository.save(new ChatParticipant(savedRoom, creator));
        chatParticipantRepository.save(new ChatParticipant(savedRoom, other));

        return ChatRoomResponse.from(savedRoom);
    }

    public List<ChatRoomResponse> getMyRooms(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return chatParticipantRepository.findByUserId(user.getId()).stream()
                .map(ChatParticipant::getChatRoom)
                .map(ChatRoomResponse::from)
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
