package com.ikhsan.securepaywallet.transaction.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikhsan.securepaywallet.enumerate.EntryDirection;
import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;
import com.ikhsan.securepaywallet.enumerate.TransactionStatus;
import com.ikhsan.securepaywallet.enumerate.TransactionType;
import com.ikhsan.securepaywallet.ledger.dto.req.EntryLine;
import com.ikhsan.securepaywallet.ledger.service.ILedger;
import com.ikhsan.securepaywallet.transaction.entity.TransactionEntity;
import com.ikhsan.securepaywallet.transaction.repo.TransactionRepository;
import com.ikhsan.securepaywallet.transaction.dto.req.TopupRequest;
import com.ikhsan.securepaywallet.transaction.dto.res.TransactionResponse;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletBalanceResponse;
import com.ikhsan.securepaywallet.wallet.service.IWallet;

@Service
public class TransactionService implements ITransaction {

    private static final String REFERENCE_PREFIX_TOPUP = "TOPUP-";

    private final TransactionRepository transactionRepository;
    private final ILedger ledger;
    private final IWallet walletService;
    private final BigDecimal maxTopupAmount;

    public TransactionService(TransactionRepository transactionRepository,
                              ILedger ledger,
                              IWallet walletService,
                              @Value("${transaction.topup.max-amount:10000000}") BigDecimal maxTopupAmount) {
        this.transactionRepository = transactionRepository;
        this.ledger = ledger;
        this.walletService = walletService;
        this.maxTopupAmount = maxTopupAmount;
    }

    @Override
    @Transactional
    public TransactionResponse topUp(UUID userId, TopupRequest request) {
        BigDecimal amount = requireValidAmount(request);

        WalletBalanceResponse wallet = walletService.increaseBalanceWithLock(userId, amount);

        TransactionEntity saved = transactionRepository.saveAndFlush(buildTransaction(amount, request.getDescription()));

        ledger.post(saved.getId(), List.of(
                new EntryLine(ledger.systemAccountId(LedgerAccountType.SYSTEM_CASH), EntryDirection.DEBIT, amount),
                new EntryLine(ledger.walletAccountId(wallet.getWalletId()), EntryDirection.CREDIT, amount)));

        return toResponse(saved);
    }

    private BigDecimal requireValidAmount(TopupRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("amount wajib diisi");
        }

        BigDecimal amount = request.getAmount();
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount harus lebih besar dari 0, diterima: " + amount);
        }
        if (amount.compareTo(maxTopupAmount) > 0) {
            throw new IllegalArgumentException("amount melebihi batas maksimum top-up: " + maxTopupAmount);
        }
        return amount;
    }

    private TransactionEntity buildTransaction(BigDecimal amount, String description) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setReferenceNumber(REFERENCE_PREFIX_TOPUP + UUID.randomUUID());
        transaction.setType(TransactionType.TOPUP);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(amount);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setDescription(description);
        return transaction;
    }

    private TransactionResponse toResponse(TransactionEntity transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
