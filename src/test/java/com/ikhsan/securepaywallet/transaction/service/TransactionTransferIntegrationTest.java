package com.ikhsan.securepaywallet.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.ikhsan.securepaywallet.transaction.dto.req.TransferRequest;
import com.ikhsan.securepaywallet.transaction.dto.res.TransactionResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.wallet.repo.WalletRepository;

class TransactionTransferIntegrationTest extends PostgresTestBase {

    private static final BigDecimal START_BALANCE = new BigDecimal("100000.00");

    @Autowired
    private ITransaction transactionService;

    @Autowired
    private ILedger ledger;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @Transactional
    @DisplayName("Transfer memindahkan uang ke penerima, bukan menambah saldo pengirim")
    void transfer_shouldMoveFundsToReceiver() {
        UserEntity sender = fundedUser(START_BALANCE);
        UserEntity receiver = newUserWithWallet();

        BigDecimal amount = new BigDecimal("30000.00");
        TransactionResponse response = transactionService.transfer(sender.getId(),
                transferRequest(receiver.getUsername(), amount));

        assertEquals("TRANSFER", response.getType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(0, response.getAmount().compareTo(amount));
        assertEquals(0, response.getFee().compareTo(BigDecimal.ZERO));

        UUID senderWallet = walletService.getWalletByUserId(sender.getId()).getId();
        UUID receiverWallet = walletService.getWalletByUserId(receiver.getId()).getId();

        assertEquals(0, balanceOf(sender).compareTo(new BigDecimal("70000.00")), "saldo pengirim salah");
        assertEquals(0, balanceOf(receiver).compareTo(amount), "saldo penerima salah");
        assertEquals(0, ledger.balanceOf(senderWallet).compareTo(new BigDecimal("70000.00")),
                "ledger pengirim tidak cocok snapshot");
        assertEquals(0, ledger.balanceOf(receiverWallet).compareTo(amount),
                "ledger penerima tidak cocok snapshot");
        assertEquals(0, balanceOf(sender).add(balanceOf(receiver)).compareTo(START_BALANCE),
                "total uang berubah (I6)");
    }

    @Test
    @Transactional
    void transfer_shouldThrowConflict_whenBalanceNotEnough() {
        UserEntity sender = fundedUser(new BigDecimal("1000.00"));
        UserEntity receiver = newUserWithWallet();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.transfer(sender.getId(),
                        transferRequest(receiver.getUsername(), new BigDecimal("5000.00"))));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(0, balanceOf(sender).compareTo(new BigDecimal("1000.00")));
        assertEquals(0, balanceOf(receiver).compareTo(BigDecimal.ZERO));
    }

    @Test
    @Transactional
    void transfer_shouldThrowBadRequest_whenReceiverIsSelf() {
        UserEntity sender = fundedUser(START_BALANCE);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(sender.getId(),
                        transferRequest(sender.getUsername(), new BigDecimal("1000.00"))));
    }

    @Test
    @Transactional
    void transfer_shouldThrowNotFound_whenReceiverUnknown() {
        UserEntity sender = fundedUser(START_BALANCE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.transfer(sender.getId(),
                        transferRequest("tidak-ada-user-ini", new BigDecimal("1000.00"))));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @Transactional
    void transfer_shouldThrowConflict_whenSenderWalletNotActive() {
        UserEntity sender = fundedUser(START_BALANCE);
        UserEntity receiver = newUserWithWallet();
        walletRepository.findByUserId(sender.getId()).orElseThrow().setStatus(WalletStatus.BLOCKED);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.transfer(sender.getId(),
                        transferRequest(receiver.getUsername(), new BigDecimal("1000.00"))));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(0, balanceOf(receiver).compareTo(BigDecimal.ZERO));
    }

    @Test
    @Transactional
    void transfer_shouldThrowBadRequest_whenAmountNotPositive() {
        UserEntity sender = fundedUser(START_BALANCE);
        UserEntity receiver = newUserWithWallet();

        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(sender.getId(),
                transferRequest(receiver.getUsername(), BigDecimal.ZERO)));
    }

    private UserEntity fundedUser(BigDecimal amount) {
        UserEntity user = newUserWithWallet();
        if (amount.signum() > 0) {
            TopupRequest topup = new TopupRequest();
            topup.setAmount(amount);
            transactionService.topUp(user.getId(), topup);
        }
        return user;
    }

    private BigDecimal balanceOf(UserEntity user) {
        return walletService.getWalletByUserId(user.getId()).getBalance();
    }

    private TransferRequest transferRequest(String receiverUsername, BigDecimal amount) {
        TransferRequest request = new TransferRequest();
        request.setReceiverUsername(receiverUsername);
        request.setAmount(amount);
        request.setDescription("integration test");
        return request;
    }
}
