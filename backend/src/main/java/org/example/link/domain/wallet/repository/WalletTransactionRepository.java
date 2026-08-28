package org.example.link.domain.wallet.repository;

import java.util.UUID;

import org.example.link.domain.wallet.entity.WalletTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransactionEntity, UUID> {
    Page<WalletTransactionEntity> findByWalletId(
            UUID walletId,
            Pageable pageable
    );
}
