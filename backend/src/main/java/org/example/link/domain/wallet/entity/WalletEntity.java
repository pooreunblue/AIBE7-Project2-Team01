package org.example.link.domain.wallet.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.common.entity.BaseEntity;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.user.entity.UserEntity;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wallets")
public class WalletEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    public WalletEntity(UserEntity user) {
        this.user = user;
        this.balance = BigDecimal.ZERO;
    }

    //충전
    public void charge(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    //결제
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        this.balance = this.balance.subtract(amount);
    }

    //정산
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        this.balance = this.balance.add(amount);
    }

    //환불
    public void refund(BigDecimal amount) {
        deposit(amount);
    }
}
