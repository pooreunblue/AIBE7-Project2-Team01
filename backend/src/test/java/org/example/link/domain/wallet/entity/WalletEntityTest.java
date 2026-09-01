package org.example.link.domain.wallet.entity;

import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletEntityTest {

    @Test
    void chargesAndWithdrawsBalance() {
        WalletEntity wallet = WalletEntity.create(user());

        wallet.charge(BigDecimal.valueOf(100_000));
        wallet.withdraw(BigDecimal.valueOf(40_000));

        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(60_000));
    }

    @Test
    void rejectsWithdrawWhenBalanceIsInsufficient() {
        WalletEntity wallet = WalletEntity.create(user());
        wallet.charge(BigDecimal.valueOf(10_000));

        assertThatThrownBy(() -> wallet.withdraw(BigDecimal.valueOf(20_000)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    void rejectsInvalidPaymentAmount() {
        WalletEntity wallet = WalletEntity.create(user());

        assertThatThrownBy(() -> wallet.withdraw(BigDecimal.ZERO))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAYMENT_AMOUNT);

        assertThatThrownBy(() -> wallet.deposit(BigDecimal.ZERO))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAYMENT_AMOUNT);
    }

    @Test
    void refundAddsBalanceLikeDeposit() {
        WalletEntity wallet = WalletEntity.create(user());

        wallet.refund(BigDecimal.valueOf(30_000));

        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(30_000));
    }

    private UserEntity user() {
        return new UserEntity("wallet@test.com", "password", "wallet-user");
    }
}
