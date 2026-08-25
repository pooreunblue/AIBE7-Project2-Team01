package org.example.link.domain.wallet.repository;

import org.example.link.domain.wallet.entity.WalletTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransactionEntity, Long> {
    Page<WalletTransactionEntity> findByWalletId(
            Long walletId,
            Pageable pageable
    );
}
