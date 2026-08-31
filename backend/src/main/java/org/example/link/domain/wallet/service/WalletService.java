package org.example.link.domain.wallet.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.trade.entity.TradeEntity;
import org.example.link.domain.wallet.dto.WalletResponse;
import org.example.link.domain.wallet.entity.WalletEntity;
import org.example.link.domain.wallet.entity.WalletTransactionEntity;
import org.example.link.domain.wallet.repository.WalletRepository;
import org.example.link.domain.wallet.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletResponse getWallet(UUID userId){
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WALLET_NOT_FOUND));
        return WalletResponse.from(wallet);
    }

    //충전
    @Transactional
    public WalletResponse charge(
            UUID userId,
            BigDecimal amount
    ) {
        WalletEntity wallet = getWalletEntityForUpdate(userId);
        wallet.charge(amount);
        walletTransactionRepository.save(
                WalletTransactionEntity.createCharge(
                        wallet,
                        amount,
                        wallet.getBalance()
                )
        );
        return WalletResponse.from(wallet);
    }

    //결제
    @Transactional
    public void withdraw(
            UUID userId,
            BigDecimal amount,
            TradeEntity trade
    ) {
        WalletEntity wallet = getWalletEntityForUpdate(userId);
        wallet.withdraw(amount);
        walletTransactionRepository.save(
                WalletTransactionEntity.createWithdraw(
                        wallet,
                        trade,
                        amount,
                        wallet.getBalance()
                )
        );
    }

    //정산
    @Transactional
    public void deposit(
            UUID userId,
            BigDecimal amount,
            TradeEntity trade
    ) {
        WalletEntity wallet = getWalletEntityForUpdate(userId);
        wallet.deposit(amount);
        walletTransactionRepository.save(
                WalletTransactionEntity.createDeposit(
                        wallet,
                        trade,
                        amount,
                        wallet.getBalance()
                )
        );
    }

    //환불
    @Transactional
    public void refund(
            UUID userId,
            BigDecimal amount,
            TradeEntity trade
    ) {
        WalletEntity wallet = getWalletEntityForUpdate(userId);

        wallet.refund(amount);

        walletTransactionRepository.save(
                WalletTransactionEntity.createRefund(
                        wallet,
                        trade,
                        amount,
                        wallet.getBalance()
                )
        );
    }

    public WalletEntity getWalletEntity(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WALLET_NOT_FOUND)
                );
    }

    private WalletEntity getWalletEntityForUpdate(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WALLET_NOT_FOUND)
                );
    }
}
