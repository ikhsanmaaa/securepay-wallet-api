package com.ikhsan.securepaywallet.ledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;

import com.ikhsan.securepaywallet.enumerate.EntryDirection;
import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;
import com.ikhsan.securepaywallet.ledger.dto.req.EntryLine;
import com.ikhsan.securepaywallet.ledger.entity.LedgerAccountEntity;
import com.ikhsan.securepaywallet.support.PostgresTestBase;

class LedgerServiceTest extends PostgresTestBase {

    @Autowired
    private ILedger ledger;

    @Test
    void post_shouldThrow_whenCalledOutsideTransaction() {
        assertThrows(IllegalTransactionStateException.class, () -> ledger.post(UUID.randomUUID(), List.of(
                new EntryLine(UUID.randomUUID(), EntryDirection.DEBIT, new BigDecimal("100.00")),
                new EntryLine(UUID.randomUUID(), EntryDirection.CREDIT, new BigDecimal("100.00")))));
    }

    @Test
    @Transactional
    void post_shouldThrow_whenTotalDebitNotEqualTotalCredit() {
        assertThrows(IllegalArgumentException.class, () -> ledger.post(UUID.randomUUID(), List.of(
                new EntryLine(UUID.randomUUID(), EntryDirection.DEBIT, new BigDecimal("100.00")),
                new EntryLine(UUID.randomUUID(), EntryDirection.CREDIT, new BigDecimal("90.00")))));
    }

    @Test
    @Transactional
    void post_shouldThrow_whenAmountNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> ledger.post(UUID.randomUUID(), List.of(
                new EntryLine(UUID.randomUUID(), EntryDirection.DEBIT, BigDecimal.ZERO),
                new EntryLine(UUID.randomUUID(), EntryDirection.CREDIT, BigDecimal.ZERO))));

        assertThrows(IllegalArgumentException.class, () -> ledger.post(UUID.randomUUID(), List.of(
                new EntryLine(UUID.randomUUID(), EntryDirection.DEBIT, new BigDecimal("-1.00")),
                new EntryLine(UUID.randomUUID(), EntryDirection.CREDIT, new BigDecimal("-1.00")))));
    }

    @Test
    @Transactional
    void post_shouldThrow_whenAmountScaleGreaterThanTwo() {
        assertThrows(IllegalArgumentException.class, () -> ledger.post(UUID.randomUUID(), List.of(
                new EntryLine(UUID.randomUUID(), EntryDirection.DEBIT, new BigDecimal("100.001")),
                new EntryLine(UUID.randomUUID(), EntryDirection.CREDIT, new BigDecimal("100.001")))));
    }

    @Test
    @Transactional
    void post_shouldThrow_whenAccountUnknown() {
        LedgerAccountEntity wallet = newWalletAccount();

        assertThrows(IllegalArgumentException.class, () -> ledger.post(UUID.randomUUID(), List.of(
                new EntryLine(UUID.randomUUID(), EntryDirection.DEBIT, new BigDecimal("10.00")),
                new EntryLine(wallet.getId(), EntryDirection.CREDIT, new BigDecimal("10.00")))));
    }

    @Test
    @Transactional
    void post_shouldAcceptBalancedEntriesWithDifferentScale() {
        LedgerAccountEntity wallet = newWalletAccount();
        LedgerAccountEntity cash = systemAccount(LedgerAccountType.SYSTEM_CASH);

        ledger.post(newTransactionId(new BigDecimal("1.00")), List.of(
                new EntryLine(cash.getId(), EntryDirection.DEBIT, new BigDecimal("1.0")),
                new EntryLine(wallet.getId(), EntryDirection.CREDIT, new BigDecimal("1.00"))));

        assertEquals(0, ledger.balanceOf(wallet.getWalletId()).compareTo(new BigDecimal("1.00")));
    }

    @Test
    @Transactional
    @DisplayName("Balance diturunkan dari entri, bukan dari snapshot wallet")
    void balanceOf_shouldSumCreditMinusDebitAcrossTransactions() {
        LedgerAccountEntity wallet = newWalletAccount();
        LedgerAccountEntity cash = systemAccount(LedgerAccountType.SYSTEM_CASH);

        ledger.post(newTransactionId(new BigDecimal("250.00")), List.of(
                new EntryLine(cash.getId(), EntryDirection.DEBIT, new BigDecimal("250.00")),
                new EntryLine(wallet.getId(), EntryDirection.CREDIT, new BigDecimal("250.00"))));

        ledger.post(newTransactionId(new BigDecimal("100.50")), List.of(
                new EntryLine(wallet.getId(), EntryDirection.DEBIT, new BigDecimal("100.50")),
                new EntryLine(cash.getId(), EntryDirection.CREDIT, new BigDecimal("100.50"))));

        assertEquals(0, ledger.balanceOf(wallet.getWalletId()).compareTo(new BigDecimal("149.50")));
    }
}
