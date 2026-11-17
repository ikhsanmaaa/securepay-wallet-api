package com.ikhsan.securepaywallet.ledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ikhsan.securepaywallet.enumerate.EntryDirection;
import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;
import com.ikhsan.securepaywallet.ledger.dto.req.EntryLine;
import com.ikhsan.securepaywallet.ledger.entity.LedgerAccountEntity;
import com.ikhsan.securepaywallet.ledger.entity.LedgerEntryEntity;
import com.ikhsan.securepaywallet.ledger.repo.LedgerEntryRepository;
import com.ikhsan.securepaywallet.support.PostgresTestBase;

class LedgerInvariantPropertyTest extends PostgresTestBase {

    private static final int OPERATIONS = 1000;
    private static final int WALLET_COUNT = 5;
    private static final long SEED = 20261115L;
    private static final long MAX_AMOUNT_CENTS = 10_000_000L;

    @Autowired
    private ILedger ledger;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Test
    @Transactional
    @DisplayName("I5 + I6: 1000 operasi acak tetap seimbang dan saldo cocok dengan ledger")
    void randomBalancedOperations_shouldKeepLedgerConsistent() {
        Random random = new Random(SEED);
        long entryCountBefore = ledgerEntryRepository.count();

        List<LedgerAccountEntity> pool = new ArrayList<>();
        for (int i = 0; i < WALLET_COUNT; i++) {
            pool.add(newWalletAccount());
        }
        pool.add(systemAccount(LedgerAccountType.SYSTEM_CASH));

        Map<UUID, BigDecimal> expectedBalance = new HashMap<>();
        pool.forEach(account -> expectedBalance.put(account.getId(), BigDecimal.ZERO));

        for (int i = 0; i < OPERATIONS; i++) {
            LedgerAccountEntity debit = pool.get(random.nextInt(pool.size()));
            LedgerAccountEntity credit;
            do {
                credit = pool.get(random.nextInt(pool.size()));
            } while (credit.getId().equals(debit.getId()));

            BigDecimal amount = BigDecimal.valueOf(random.nextLong(1, MAX_AMOUNT_CENTS), 2);

            ledger.post(newTransactionId(amount), List.of(
                    new EntryLine(debit.getId(), EntryDirection.DEBIT, amount),
                    new EntryLine(credit.getId(), EntryDirection.CREDIT, amount)));

            expectedBalance.merge(debit.getId(), amount.negate(), BigDecimal::add);
            expectedBalance.merge(credit.getId(), amount, BigDecimal::add);
        }

        assertEquals(entryCountBefore + (long) OPERATIONS * 2, ledgerEntryRepository.count());

        expectedBalance.forEach((accountId, expected) -> {
            BigDecimal actual = ledgerEntryRepository.sumBalanceByAccountId(accountId, EntryDirection.CREDIT);
            assertEquals(0, (actual == null ? BigDecimal.ZERO : actual).compareTo(expected),
                    "saldo akun tidak cocok dengan ledger: " + accountId);
        });

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (LedgerEntryEntity entry : ledgerEntryRepository.findAll()) {
            if (entry.getDirection() == EntryDirection.DEBIT) {
                totalDebit = totalDebit.add(entry.getAmount());
            } else {
                totalCredit = totalCredit.add(entry.getAmount());
            }
        }
        assertEquals(0, totalDebit.compareTo(totalCredit),
                "I6 dilanggar: total debit=" + totalDebit + " total kredit=" + totalCredit);

        for (LedgerAccountEntity wallet : pool) {
            if (wallet.getWalletId() == null) {
                continue;
            }
            assertEquals(0, ledger.balanceOf(wallet.getWalletId())
                            .compareTo(expectedBalance.get(wallet.getId())),
                    "balanceOf tidak cocok dengan ledger: " + wallet.getWalletId());
        }
    }
}
