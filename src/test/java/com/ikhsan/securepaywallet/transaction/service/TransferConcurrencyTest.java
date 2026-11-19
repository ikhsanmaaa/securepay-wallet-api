package com.ikhsan.securepaywallet.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ikhsan.securepaywallet.ledger.service.ILedger;
import com.ikhsan.securepaywallet.support.PostgresTestBase;
import com.ikhsan.securepaywallet.transaction.dto.req.TopupRequest;
import com.ikhsan.securepaywallet.transaction.dto.req.TransferRequest;
import com.ikhsan.securepaywallet.user.entity.UserEntity;

class TransferConcurrencyTest extends PostgresTestBase {

    private static final int OPERATIONS = 100;
    private static final int THREADS = 16;
    private static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("1000.00");

    @Autowired
    private ITransaction transactionService;

    @Autowired
    private ILedger ledger;

    @Test
    @DisplayName("100 transfer paralel dua arah: tanpa deadlock, tanpa lost update, tanpa saldo minus")
    void concurrentBidirectionalTransfers_shouldStayConsistent() throws Exception {
        UserEntity a = newUserWithWallet();
        UserEntity b = newUserWithWallet();
        topUp(a, new BigDecimal("100000.00"));
        topUp(b, new BigDecimal("50000.00"));

        UUID walletA = walletService.getWalletByUserId(a.getId()).getId();
        UUID walletB = walletService.getWalletByUserId(b.getId()).getId();
        BigDecimal totalBefore = balanceOf(a).add(balanceOf(b));

        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        List<String> failures = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < OPERATIONS; i++) {
            boolean aToB = i % 2 == 0;
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    if (aToB) {
                        transactionService.transfer(a.getId(), transferRequest(b.getUsername()));
                    } else {
                        transactionService.transfer(b.getId(), transferRequest(a.getUsername()));
                    }
                    succeeded.incrementAndGet();
                } catch (Exception e) {
                    failures.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                return null;
            }));
        }

        long startedAt = System.currentTimeMillis();
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(120, TimeUnit.SECONDS), "masih ada thread yang belum selesai");
        long elapsed = System.currentTimeMillis() - startedAt;

        for (Future<?> future : futures) {
            future.get();
        }

        assertTrue(failures.isEmpty(), "ada transfer gagal (" + failures.size() + "): " + failures);
        assertEquals(OPERATIONS, succeeded.get());
        BigDecimal totalAfter = balanceOf(a).add(balanceOf(b));
        String diag = "totalBefore=" + totalBefore + " totalAfter=" + totalAfter + " A=" + balanceOf(a)
                + " B=" + balanceOf(b) + " ledgerA=" + ledger.balanceOf(walletA) + " ledgerB=" + ledger.balanceOf(walletB);
        assertEquals(0, totalAfter.compareTo(totalBefore), "total uang berubah (I6) | " + diag);
        assertTrue(balanceOf(a).signum() >= 0, "saldo A negatif: " + balanceOf(a));
        assertTrue(balanceOf(b).signum() >= 0, "saldo B negatif: " + balanceOf(b));
        assertEquals(0, balanceOf(a).compareTo(ledger.balanceOf(walletA)), "snapshot A != ledger A | " + diag);
        assertEquals(0, balanceOf(b).compareTo(ledger.balanceOf(walletB)), "snapshot B != ledger B | " + diag);

        System.out.println("PARALEL: " + OPERATIONS + " transfer dalam " + elapsed + " ms | " + diag);
    }

    private void topUp(UserEntity user, BigDecimal amount) {
        TopupRequest request = new TopupRequest();
        request.setAmount(amount);
        transactionService.topUp(user.getId(), request);
    }

    private TransferRequest transferRequest(String receiverUsername) {
        TransferRequest request = new TransferRequest();
        request.setReceiverUsername(receiverUsername);
        request.setAmount(TRANSFER_AMOUNT);
        return request;
    }

    private BigDecimal balanceOf(UserEntity user) {
        return walletService.getWalletByUserId(user.getId()).getBalance();
    }
}
