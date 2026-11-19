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

        Map<UUID, BigDecimal> startBalance = new HashMap<>();
        Map<UUID, BigDecimal> delta = new HashMap<>();
        pool.forEach(account -> {
            startBalance.put(account.getId(), currentLedgerBalance(account.getId()));
            delta.put(account.getId(), BigDecimal.ZERO);
        });

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

            delta.merge(debit.getId(), amount.negate(), BigDecimal::add);
            delta.merge(credit.getId(), amount, BigDecimal::add);
        }

        assertEquals(entryCountBefore + (long) OPERATIONS * 2, ledgerEntryRepository.count());

        pool.forEach(account -> {
            BigDecimal expected = startBalance.get(account.getId()).add(delta.get(account.getId()));
            assertEquals(0, currentLedgerBalance(account.getId()).compareTo(expected),
                    "saldo akun tidak cocok dengan ledger: " + account.getId());
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
            BigDecimal expected = startBalance.get(wallet.getId()).add(delta.get(wallet.getId()));
            assertEquals(0, ledger.balanceOf(wallet.getWalletId()).compareTo(expected),
                    "balanceOf tidak cocok dengan ledger: " + wallet.getWalletId());
        }
    }

    private BigDecimal currentLedgerBalance(UUID accountId) {
        BigDecimal balance = ledgerEntryRepository.sumBalanceByAccountId(accountId, EntryDirection.CREDIT);
        return balance == null ? BigDecimal.ZERO : balance;
    }
}
