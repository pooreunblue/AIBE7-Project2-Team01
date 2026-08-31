package org.example.link.domain.request.entity;

import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.request.util.RequestPostStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestPostEntityTest {

    @Test
    void progressesFromOpenToInProgressAndClosed() {
        RequestPostEntity post = requestPost(RequestPostStatus.OPEN);

        post.startTrade();
        assertThat(post.getStatus()).isEqualTo(RequestPostStatus.IN_PROGRESS);

        post.completeTrade();
        assertThat(post.getStatus()).isEqualTo(RequestPostStatus.CLOSED);
    }

    @Test
    void reopensWhenPaidTradeIsCancelled() {
        RequestPostEntity post = requestPost(RequestPostStatus.IN_PROGRESS);

        post.reopenAfterTradeCancellation();

        assertThat(post.getStatus()).isEqualTo(RequestPostStatus.OPEN);
    }

    @Test
    void rejectsStartingAnotherTradeUnlessPostIsOpen() {
        RequestPostEntity post = requestPost(RequestPostStatus.IN_PROGRESS);

        assertThatThrownBy(post::startTrade)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST_POST_STATUS);
    }

    @Test
    void allowsManualCloseAndCancellationOnlyWhileOpen() {
        RequestPostEntity closedPost = requestPost(RequestPostStatus.OPEN);
        RequestPostEntity cancelledPost = requestPost(RequestPostStatus.OPEN);

        closedPost.closeStatus();
        cancelledPost.cancelStatus();

        assertThat(closedPost.getStatus()).isEqualTo(RequestPostStatus.CLOSED);
        assertThat(cancelledPost.getStatus()).isEqualTo(RequestPostStatus.CANCELLED);
    }

    private RequestPostEntity requestPost(RequestPostStatus status) {
        return RequestPostEntity.builder()
                .status(status)
                .build();
    }
}
