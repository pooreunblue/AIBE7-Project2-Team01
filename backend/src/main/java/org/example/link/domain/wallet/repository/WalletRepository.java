package org.example.link.domain.wallet.repository;

import jakarta.persistence.LockModeType;
import java.util.UUID;

import org.example.link.domain.wallet.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletEntity w where w.user.id = :userId")
    Optional<WalletEntity> findByUserIdForUpdate(@Param("userId") UUID userId);
}
