package org.example.link.domain.wallet.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
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

    public WalletResponse getWallet(Long userId){
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WALLET_NOT_FOUND));
        return WalletResponse.from(wallet);
    }

    @Transactional
    public WalletResponse charge(
            Long userId,
            BigDecimal amount
    ){
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WALLET_NOT_FOUND));
        wallet.charge(amount);

        WalletTransactionEntity transaction =
                WalletTransactionEntity.createCharge(
                        wallet,
                        amount,
                        wallet.getBalance()
                );

        walletTransactionRepository.save(transaction);
        return WalletResponse.from(wallet);
    }
}
