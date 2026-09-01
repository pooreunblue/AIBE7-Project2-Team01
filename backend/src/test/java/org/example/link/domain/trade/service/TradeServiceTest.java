package org.example.link.domain.trade.service;

import org.example.link.ai.embedding.event.EmbeddingEventPublisher;
import org.example.link.domain.chat.entity.ChatParticipant;
import org.example.link.domain.chat.entity.ChatRoom;
import org.example.link.domain.chat.repository.ChatParticipantRepository;
import org.example.link.domain.chat.repository.ChatRoomRepository;
import org.example.link.domain.chat.service.ChatMessagePublisher;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.request.util.RequestPostStatus;
import org.example.link.domain.trade.dto.TradeCreateRequest;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.trade.entity.TradeStatus;
import org.example.link.domain.trade.repository.TradeRepository;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.example.link.domain.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private RequestPostRepository requestPostRepository;
    @Mock
    private TalentPostRepository talentPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatMessagePublisher chatMessagePublisher;
    @Mock
    private WalletService walletService;
    @Mock
    private EmbeddingEventPublisher embeddingEventPublisher;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeService = new TradeService(
                tradeRepository,
                chatRoomRepository,
                chatParticipantRepository,
                requestPostRepository,
                talentPostRepository,
                userRepository,
                chatMessagePublisher,
                walletService,
                embeddingEventPublisher
        );
    }

    @Test
    void createsRequestTradeAndStartsRequestPost() {
        UUID chatRoomId = UUID.randomUUID();
        UUID requestPostId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        UUID payeeId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(requestPostId, null);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        UserEntity payer = user(payerId);
        UserEntity payee = user(payeeId);
        RequestPostEntity requestPost = requestPost(RequestPostStatus.OPEN, requestPostId, payer);

        when(chatRoomRepository.findByIdForUpdate(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.existsByChatRoomIdAndUserId(chatRoomId, payeeId)).thenReturn(true);
        when(tradeRepository.existsByChatRoomIdAndStatusIn(any(), any())).thenReturn(false);
        when(requestPostRepository.findByIdForUpdate(requestPostId)).thenReturn(Optional.of(requestPost));
        when(chatParticipantRepository.findByChatRoomId(chatRoomId)).thenReturn(List.of(
                new ChatParticipant(chatRoom, payer),
                new ChatParticipant(chatRoom, payee)
        ));
        when(tradeRepository.save(any(TradeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(payeeId)).thenReturn(Optional.of(payee));

        tradeService.createTrade(
                payeeId,
                chatRoomId,
                new TradeCreateRequest(BigDecimal.valueOf(100_000), requestPostId, null)
        );

        assertThat(requestPost.getStatus()).isEqualTo(RequestPostStatus.IN_PROGRESS);
        verify(embeddingEventPublisher).deleteRequest(requestPostId);
    }

    @Test
    void paysRequestTradeAndCompletesImmediately() {
        UUID tradeId = UUID.randomUUID();
        UUID chatRoomId = UUID.randomUUID();
        UUID requestPostId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        TradeEntity trade = requestTrade(chatRoomId, requestPostId, payerId);
        RequestPostEntity requestPost = requestPost(RequestPostStatus.IN_PROGRESS);

        when(tradeRepository.findByIdForUpdate(tradeId)).thenReturn(Optional.of(trade));
        when(requestPostRepository.findByIdForUpdate(requestPostId)).thenReturn(Optional.of(requestPost));
        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(org.mockito.Mockito.mock(ChatRoom.class)));
        when(userRepository.findById(payerId)).thenReturn(Optional.of(org.mockito.Mockito.mock(UserEntity.class)));

        tradeService.pay(payerId, tradeId);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(requestPost.getStatus()).isEqualTo(RequestPostStatus.CLOSED);
        verify(walletService).withdraw(payerId, trade.getAmount(), trade);
        verify(walletService).deposit(trade.getPayeeId(), trade.getAmount(), trade);
    }

    @Test
    void completesRequestTradeAndClosesRequestPost() {
        UUID tradeId = UUID.randomUUID();
        UUID requestPostId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        TradeEntity trade = requestTrade(UUID.randomUUID(), requestPostId, payerId);
        trade.paid();
        RequestPostEntity requestPost = requestPost(RequestPostStatus.IN_PROGRESS);

        when(tradeRepository.findByIdForUpdate(tradeId)).thenReturn(Optional.of(trade));
        when(requestPostRepository.findByIdForUpdate(requestPostId)).thenReturn(Optional.of(requestPost));

        tradeService.complete(payerId, tradeId);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(requestPost.getStatus()).isEqualTo(RequestPostStatus.CLOSED);
        verify(walletService).deposit(trade.getPayeeId(), trade.getAmount(), trade);
    }

    @Test
    void cancelsPaidRequestTradeAndReopensRequestPost() {
        UUID tradeId = UUID.randomUUID();
        UUID requestPostId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        TradeEntity trade = requestTrade(UUID.randomUUID(), requestPostId, payerId);
        trade.paid();
        RequestPostEntity requestPost = requestPost(RequestPostStatus.IN_PROGRESS);

        when(tradeRepository.findByIdForUpdate(tradeId)).thenReturn(Optional.of(trade));
        when(requestPostRepository.findByIdForUpdate(requestPostId)).thenReturn(Optional.of(requestPost));

        tradeService.cancel(payerId, tradeId);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(requestPost.getStatus()).isEqualTo(RequestPostStatus.OPEN);
        verify(walletService).refund(payerId, trade.getAmount(), trade);
        verify(embeddingEventPublisher).saveRequest(requestPost);
    }

    @Test
    void cancelsPendingRequestTradeAndReopensRequestPost() {
        UUID tradeId = UUID.randomUUID();
        UUID requestPostId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        TradeEntity trade = requestTrade(UUID.randomUUID(), requestPostId, payerId);
        RequestPostEntity requestPost = requestPost(RequestPostStatus.IN_PROGRESS);

        when(tradeRepository.findByIdForUpdate(tradeId)).thenReturn(Optional.of(trade));
        when(requestPostRepository.findByIdForUpdate(requestPostId)).thenReturn(Optional.of(requestPost));

        tradeService.cancel(trade.getPayeeId(), tradeId);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.CANCELLED);
        assertThat(requestPost.getStatus()).isEqualTo(RequestPostStatus.OPEN);
        verify(embeddingEventPublisher).saveRequest(requestPost);
        verifyNoInteractions(walletService);
    }

    private TradeEntity requestTrade(UUID chatRoomId, UUID requestPostId, UUID payerId) {
        return new TradeEntity(
                chatRoomId,
                requestPostId,
                null,
                payerId,
                UUID.randomUUID(),
                BigDecimal.valueOf(100_000)
        );
    }

    private RequestPostEntity requestPost(RequestPostStatus status) {
        return RequestPostEntity.builder()
                .status(status)
                .build();
    }

    private RequestPostEntity requestPost(RequestPostStatus status, UUID requestPostId, UserEntity user) {
        RequestPostEntity requestPost = RequestPostEntity.builder()
                .user(user)
                .status(status)
                .build();
        ReflectionTestUtils.setField(requestPost, "id", requestPostId);
        return requestPost;
    }

    private UserEntity user(UUID userId) {
        UserEntity user = new UserEntity("user-" + userId + "@test.com", "password", "nickname-" + userId);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
