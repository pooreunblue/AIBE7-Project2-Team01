package org.example.link.domain.trade.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.ai.embedding.event.EmbeddingEventPublisher;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.chat.entity.ChatParticipant;
import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.chat.repository.ChatParticipantRepository;
import org.example.link.domain.chat.repository.ChatRoomRepository;
import org.example.link.domain.chat.service.ChatMessagePublisher;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.request.util.RequestPostStatus;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.example.link.domain.talent.util.TalentPostStatus;
import org.example.link.domain.trade.dto.TradeCreateRequest;
import org.example.link.domain.trade.dto.TradeResponse;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;
import org.example.link.domain.trade.repository.TradeRepository;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
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
    private final RequestPostRepository requestPostRepository;
    private final TalentPostRepository talentPostRepository;
    private final UserRepository userRepository;
    private final ChatMessagePublisher chatMessagePublisher;
    private final WalletService walletService;
    private final EmbeddingEventPublisher embeddingEventPublisher;

    private static final String TRADE_REQUEST_MESSAGE = "거래를 요청했습니다.";
    private static final String TRADE_PAID_MESSAGE = "결제가 완료되었습니다.";
    private static final String TRADE_COMPLETED_MESSAGE = "거래 완료되었습니다.";

    @Transactional
    public TradeResponse createTrade(UUID userId, UUID chatRoomId, TradeCreateRequest request) {
        boolean hasRequestPost = request.requestPostId() != null;
        boolean hasTalentPost = request.talentPostId() != null;
        if (hasRequestPost == hasTalentPost) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        if (tradeRepository.existsByChatRoomIdAndStatusIn(chatRoomId, ACTIVE_STATUSES)) {
            throw new CustomException(ErrorCode.TRADE_ALREADY_IN_PROGRESS);
        }

        TradeParties parties = resolveParties(userId, chatRoom, request, hasRequestPost);

        TradeEntity trade = new TradeEntity(
                chatRoom.getId(),
                request.requestPostId(),
                request.talentPostId(),
                parties.payerId(),
                parties.payeeId(),
                request.amount()
        );
        TradeEntity saved = tradeRepository.save(trade);

        // 거래 요청 성공 시 채팅방에 거래 요청 카드 메시지를 자동 저장 + 브로드캐스트.
        UserEntity requester = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        chatMessagePublisher.publishTradeRequest(chatRoom, requester, TRADE_REQUEST_MESSAGE, saved);

        return TradeResponse.from(saved);
    }

    private record TradeParties(UUID payerId, UUID payeeId) {
    }

    /**
     * 게시글 타입별로 결제자(payer)/수취자(payee)를 결정한다.
     * - 요청글(의뢰): 글쓴이(의뢰인)가 결제자, 채팅 상대(수락한 전문가)가 수취자.
     * - 재능글: 채팅 상대(구매자)가 결제자, 글쓴이(전문가)가 수취자.
     * 금액을 확정하는 수취자가 거래를 생성하며, 게시글이 채팅방과 실제로 연결돼 있어야 한다.
     */
    private TradeParties resolveParties(UUID userId, ChatRoom chatRoom, TradeCreateRequest request, boolean isRequestPost) {
        if (isRequestPost) {
            if (!request.requestPostId().equals(chatRoom.getRequestPostId())) {
                throw new CustomException(ErrorCode.CHATROOM_POST_MISMATCH);
            }
            RequestPostEntity post = requestPostRepository.findByIdForUpdate(request.requestPostId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
            if (post.getStatus() != RequestPostStatus.OPEN) {
                throw new CustomException(ErrorCode.INVALID_REQUEST_POST_STATUS);
            }
            UUID ownerId = post.getUser().getId();
            UUID payeeId = findCounterpartId(chatRoom.getId(), ownerId);
            if (!userId.equals(payeeId)) {
                throw new CustomException(ErrorCode.TRADE_CREATE_ACCESS_DENIED);
            }
            return new TradeParties(ownerId, payeeId);
        }

        if (!request.talentPostId().equals(chatRoom.getTalentPostId())) {
            throw new CustomException(ErrorCode.CHATROOM_POST_MISMATCH);
        }
        TalentPostEntity post = talentPostRepository.findById(request.talentPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.TALENT_POST_NOT_FOUND));
        if (post.getStatus() != TalentPostStatus.ACTIVE) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        UUID ownerId = post.getUser().getId();
        if (!userId.equals(ownerId)) {
            throw new CustomException(ErrorCode.TRADE_CREATE_ACCESS_DENIED);
        }
        return new TradeParties(findCounterpartId(chatRoom.getId(), ownerId), ownerId);
    }

    public TradeResponse getTrade(UUID userId, UUID tradeId) {
        TradeEntity trade = getOwnedTrade(userId, tradeId);
        return TradeResponse.from(trade);
    }

    public Page<TradeResponse> getMyTrades(UUID userId, Pageable pageable) {
        return tradeRepository.findByPayerIdOrPayeeId(userId, userId, pageable)
                .map(TradeResponse::from);
    }

    @Transactional
    public TradeResponse pay(UUID userId, UUID tradeId) {
        TradeEntity trade = getTradeForUpdate(tradeId);
        if (!trade.getPayerId().equals(userId)) {
            throw new CustomException(ErrorCode.TRADE_ACCESS_DENIED);
        }
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_TRADE_STATUS);
        }

        startRequestTradeIfPresent(trade);
        walletService.withdraw(trade.getPayerId(), trade.getAmount(), trade);
        trade.paid();

        ChatRoom chatRoom = chatRoomRepository.findById(trade.getChatRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        UserEntity payer = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        chatMessagePublisher.publishTradePaid(chatRoom, payer, TRADE_PAID_MESSAGE, trade);

        return TradeResponse.from(trade);
    }

    @Transactional
    public TradeResponse complete(UUID userId, UUID tradeId) {
        TradeEntity trade = getTradeForUpdate(tradeId);
        if (!trade.getPayerId().equals(userId)) {
            throw new CustomException(ErrorCode.TRADE_ACCESS_DENIED);
        }
        if (trade.getStatus() != TradeStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_TRADE_STATUS);
        }

        completeRequestTradeIfPresent(trade);
        walletService.deposit(trade.getPayeeId(), trade.getAmount(), trade);
        trade.complete();

        publishTradeCompletedMessage(userId, trade);

        return TradeResponse.from(trade);
    }

    @Transactional
    public TradeResponse cancel(UUID userId, UUID tradeId) {
        TradeEntity trade = getOwnedTradeForUpdate(userId, tradeId);
        if (trade.getStatus() != TradeStatus.PENDING && trade.getStatus() != TradeStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_TRADE_STATUS);
        }

        if (trade.getStatus() == TradeStatus.PAID) {
            reopenRequestTradeIfPresent(trade);
            walletService.refund(trade.getPayerId(), trade.getAmount(), trade);
        }
        trade.cancel();
        return TradeResponse.from(trade);
    }

    private TradeEntity getOwnedTrade(UUID userId, UUID tradeId) {
        TradeEntity trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRADE_NOT_FOUND));
        validateTradeOwner(userId, trade);
        return trade;
    }

    private TradeEntity getOwnedTradeForUpdate(UUID userId, UUID tradeId) {
        TradeEntity trade = getTradeForUpdate(tradeId);
        validateTradeOwner(userId, trade);
        return trade;
    }

    private TradeEntity getTradeForUpdate(UUID tradeId) {
        return tradeRepository.findByIdForUpdate(tradeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRADE_NOT_FOUND));
    }

    private void validateTradeOwner(UUID userId, TradeEntity trade) {
        if (!trade.getPayerId().equals(userId) && !trade.getPayeeId().equals(userId)) {
            throw new CustomException(ErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    private void publishTradeCompletedMessage(UUID userId, TradeEntity trade) {
        chatRoomRepository.findById(trade.getChatRoomId()).ifPresent(chatRoom ->
                userRepository.findById(userId).ifPresent(completer ->
                        chatMessagePublisher.publishTradeCompleted(
                                chatRoom,
                                completer,
                                TRADE_COMPLETED_MESSAGE,
                                trade
                        )
                )
        );
    }

    private void startRequestTradeIfPresent(TradeEntity trade) {
        if (trade.getRequestPostId() == null) {
            return;
        }
        RequestPostEntity requestPost = getRequestPostForUpdate(trade.getRequestPostId());
        requestPost.startTrade();
        embeddingEventPublisher.deleteRequest(trade.getRequestPostId());
    }

    private void completeRequestTradeIfPresent(TradeEntity trade) {
        if (trade.getRequestPostId() == null) {
            return;
        }
        RequestPostEntity requestPost = getRequestPostForUpdate(trade.getRequestPostId());
        requestPost.completeTrade();
    }

    private void reopenRequestTradeIfPresent(TradeEntity trade) {
        if (trade.getRequestPostId() == null) {
            return;
        }
        RequestPostEntity requestPost = getRequestPostForUpdate(trade.getRequestPostId());
        requestPost.reopenAfterTradeCancellation();
        embeddingEventPublisher.saveRequest(requestPost);
    }

    private RequestPostEntity getRequestPostForUpdate(UUID requestPostId) {
        return requestPostRepository.findByIdForUpdate(requestPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private UUID findCounterpartId(UUID chatRoomId, UUID excludeUserId) {
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoomId(chatRoomId);
        return participants.stream()
                .map(p -> p.getUser().getId())
                .filter(id -> !id.equals(excludeUserId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED));
    }
}
