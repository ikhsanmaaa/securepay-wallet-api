package com.ikhsan.securepaywallet.ledger.repo;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ikhsan.securepaywallet.enumerate.EntryDirection;
import com.ikhsan.securepaywallet.ledger.entity.LedgerEntryEntity;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    @Query("""
           select sum(case when e.direction = :credit then e.amount else -e.amount end)
           from LedgerEntryEntity e
           where e.account.id = :accountId
           """)
    BigDecimal sumBalanceByAccountId(@Param("accountId") UUID accountId,
                                     @Param("credit") EntryDirection credit);
}
