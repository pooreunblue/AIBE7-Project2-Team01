package org.example.link.domain.trade.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.chat.entity.ChatParticipant;
import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.chat.repository.ChatMessageRepository;
import org.example.link.domain.chat.repository.ChatParticipantRepository;
import org.example.link.domain.chat.repository.ChatRoomRepository;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.trade.dto.TradeCreateRequest;
import org.example.link.domain.trade.dto.TradeResponse;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;
import org.example.link.domain.trade.repository.TradeRepository;
import org.example.link.domain.wallet.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private static final List<TradeStatus> ACTIVE_STATUSES = List.of(TradeStatus.PENDING, TradeStatus.PAID);

    private final TradeRepository tradeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RequestPostRepository requestPostRepository;
    private final WalletService walletService;

    @Transactional
    public TradeResponse createTrade(Long userId, Long chatRoomId, TradeCreateRequest request) {
        boolean hasRequestPost = request.requestPostId() != null;
        boolean hasTalentPost = request.talentPostId() != null;
        if (hasRequestPost == hasTalentPost) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (hasTalentPost) {
            // talent 도메인이 아직 없어서 게시글 유효성/작성자를 확인할 방법이 없음 — 도메인 완성 전까지는 막아둠.
            throw new CustomException(ErrorCode.TALENT_TRADE_NOT_SUPPORTED);
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        if (!chatMessageRepository.existsByChatRoomId(chatRoomId)) {
            throw new CustomException(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
        }

        if (tradeRepository.existsByChatRoomIdAndStatusIn(chatRoomId, ACTIVE_STATUSES)) {
            throw new CustomException(ErrorCode.TRADE_ALREADY_IN_PROGRESS);
        }

        RequestPostEntity requestPost = requestPostRepository.findById(request.requestPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 요청글(의뢰) 기준: 글쓴이(의뢰인)가 결제자, 채팅 상대(수락한 전문가)가 수취자.
        Long postOwnerId = requestPost.getUser().getId();
        Long counterpartId = findCounterpartId(chatRoomId, postOwnerId);

        TradeEntity trade = new TradeEntity(
                chatRoom.getId(),
                request.requestPostId(),
                request.talentPostId(),
                postOwnerId,
                counterpartId,
                request.amount()
        );
        TradeEntity saved = tradeRepository.save(trade);
        return TradeResponse.from(saved);
    }

    public TradeResponse getTrade(Long userId, Long tradeId) {
        TradeEntity trade = getOwnedTrade(userId, tradeId);
        return TradeResponse.from(trade);
    }

    public Page<TradeResponse> getMyTrades(Long userId, Pageable pageable) {
        return tradeRepository.findByPayerIdOrPayeeId(userId, userId, pageable)
                .map(TradeResponse::from);
    }

    @Transactional
    public TradeResponse pay(Long userId, Long tradeId) {
        TradeEntity trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRADE_NOT_FOUND));
        if (!trade.getPayerId().equals(userId)) {
            throw new CustomException(ErrorCode.TRADE_ACCESS_DENIED);
        }
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_TRADE_STATUS);
        }

        walletService.withdraw(trade.getPayerId(), trade.getAmount(), trade);
        trade.paid();
        return TradeResponse.from(trade);
    }

    @Transactional
    public TradeResponse complete(Long userId, Long tradeId) {
        TradeEntity trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRADE_NOT_FOUND));
        if (!trade.getPayerId().equals(userId)) {
            throw new CustomException(ErrorCode.TRADE_ACCESS_DENIED);
        }
        if (trade.getStatus() != TradeStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_TRADE_STATUS);
        }

        walletService.deposit(trade.getPayeeId(), trade.getAmount(), trade);
        trade.complete();
        return TradeResponse.from(trade);
    }

    @Transactional
    public TradeResponse cancel(Long userId, Long tradeId) {
        TradeEntity trade = getOwnedTrade(userId, tradeId);
        if (trade.getStatus() != TradeStatus.PENDING && trade.getStatus() != TradeStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_TRADE_STATUS);
        }

        if (trade.getStatus() == TradeStatus.PAID) {
            walletService.refund(trade.getPayerId(), trade.getAmount(), trade);
        }
        trade.cancel();
        return TradeResponse.from(trade);
    }

    private TradeEntity getOwnedTrade(Long userId, Long tradeId) {
        TradeEntity trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRADE_NOT_FOUND));
        if (!trade.getPayerId().equals(userId) && !trade.getPayeeId().equals(userId)) {
            throw new CustomException(ErrorCode.TRADE_ACCESS_DENIED);
        }
        return trade;
    }

    private Long findCounterpartId(Long chatRoomId, Long excludeUserId) {
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoomId(chatRoomId);
        return participants.stream()
                .map(p -> p.getUser().getId())
                .filter(id -> !id.equals(excludeUserId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED));
    }
}
