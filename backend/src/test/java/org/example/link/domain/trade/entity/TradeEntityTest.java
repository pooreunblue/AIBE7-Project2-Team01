package org.example.link.domain.trade.entity;

import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeEntityTest {

    @Test
    void progressesFromPendingToPaidAndCompleted() {
        TradeEntity trade = trade();

        trade.paid();
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.PAID);

        trade.complete();
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
    }

    @Test
    void rejectsCompletionBeforePayment() {
        TradeEntity trade = trade();

        assertThatThrownBy(trade::complete)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TRADE_STATUS);
    }

    @Test
    void rejectsCancellingCompletedTrade() {
        TradeEntity trade = trade();
        trade.paid();
        trade.complete();

        assertThatThrownBy(trade::cancel)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TRADE_STATUS);
    }

    private TradeEntity trade() {
        return new TradeEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100_000)
        );
    }
}
