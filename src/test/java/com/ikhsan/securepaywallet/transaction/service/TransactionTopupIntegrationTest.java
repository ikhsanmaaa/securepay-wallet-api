package com.ikhsan.securepaywallet.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.enumerate.WalletStatus;
import com.ikhsan.securepaywallet.ledger.service.ILedger;
import com.ikhsan.securepaywallet.support.PostgresTestBase;
import com.ikhsan.securepaywallet.transaction.dto.req.TopupRequest;
import com.ikhsan.securepaywallet.transaction.dto.res.TransactionResponse;
import com.ikhsan.securepaywallet.wallet.repo.WalletRepository;

class TransactionTopupIntegrationTest extends PostgresTestBase {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000");

    @Autowired
    private ITransaction transactionService;

    @Autowired
    private ILedger ledger;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @Transactional
    @DisplayName("Top-up menambah snapshot dan ledger secara konsisten (I5)")
    void topUp_shouldIncreaseSnapshotAndLedgerBySameAmount() {
        UUID userId = newUserWithWallet().getId();
        UUID walletId = walletService.getWalletByUserId(userId).getId();
        BigDecimal amount = new BigDecimal("100000.00");

        TransactionResponse response = transactionService.topUp(userId, topupRequest(amount));

        assertNotNull(response.getId());
        assertNotNull(response.getReferenceNumber());
        assertEquals("TOPUP", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(0, response.getAmount().compareTo(amount));
        assertEquals(0, response.getFee().compareTo(BigDecimal.ZERO));

        assertEquals(0, walletService.getWalletByUserId(userId).getBalance().compareTo(amount),
                "snapshot wallet tidak bertambah tepat");
        assertEquals(0, ledger.balanceOf(walletId).compareTo(amount),
                "saldo ledger tidak sama dengan snapshot (I5)");
    }

    @Test
    @Transactional
    void topUp_shouldAccumulateBalance_whenCalledTwice() {
        UUID userId = newUserWithWallet().getId();

        transactionService.topUp(userId, topupRequest(new BigDecimal("250.50")));
        transactionService.topUp(userId, topupRequest(new BigDecimal("149.50")));

        assertEquals(0, walletService.getWalletByUserId(userId).getBalance().compareTo(new BigDecimal("400.00")));
    }

    @Test
    @Transactional
    void topUp_shouldThrowBadRequest_whenAmountNotPositive() {
        UUID userId = newUserWithWallet().getId();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.topUp(userId, topupRequest(BigDecimal.ZERO)));
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.topUp(userId, topupRequest(new BigDecimal("-1.00"))));
    }

    @Test
    @Transactional
    void topUp_shouldThrowBadRequest_whenAmountAboveMaximum() {
        UUID userId = newUserWithWallet().getId();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.topUp(userId, topupRequest(MAX_AMOUNT.add(new BigDecimal("0.01")))));

        assertEquals(0, walletService.getWalletByUserId(userId).getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    @Transactional
    void topUp_shouldThrowNotFound_whenWalletMissing() {
        UUID userId = newUser().getId();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.topUp(userId, topupRequest(new BigDecimal("10.00"))));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @Transactional
    void topUp_shouldThrowConflict_whenWalletNotActive() {
        UUID userId = newUserWithWallet().getId();
        walletRepository.findByUserId(userId).orElseThrow().setStatus(WalletStatus.BLOCKED);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.topUp(userId, topupRequest(new BigDecimal("10.00"))));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private TopupRequest topupRequest(BigDecimal amount) {
        TopupRequest request = new TopupRequest();
        request.setAmount(amount);
        request.setDescription("integration test");
        return request;
    }
}
