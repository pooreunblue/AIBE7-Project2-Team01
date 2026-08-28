package org.example.link.domain.wallet.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.wallet.dto.WalletTransactionResponse;
import org.example.link.domain.wallet.entity.WalletEntity;
import org.example.link.domain.wallet.entity.WalletTransactionEntity;
import org.example.link.domain.wallet.repository.WalletRepository;
import org.example.link.domain.wallet.repository.WalletTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public Page<WalletTransactionResponse> getTransactions(
            UUID userId,
            Pageable pageable
    ) {
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WALLET_NOT_FOUND)
                );
        return walletTransactionRepository
                .findByWalletId(
                        wallet.getId(),
                        pageable
                )
                .map(WalletTransactionResponse::from);
    }
}
