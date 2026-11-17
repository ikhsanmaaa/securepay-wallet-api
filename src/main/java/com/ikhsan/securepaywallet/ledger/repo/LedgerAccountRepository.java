package com.ikhsan.securepaywallet.ledger.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ikhsan.securepaywallet.ledger.entity.LedgerAccountEntity;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccountEntity, UUID> {
    Optional<LedgerAccountEntity> findByCode(String code);

    Optional<LedgerAccountEntity> findByWalletId(UUID walletId);
}
