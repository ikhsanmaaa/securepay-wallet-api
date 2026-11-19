package com.ikhsan.securepaywallet.transaction.service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.ikhsan.securepaywallet.transaction.dto.req.TopupRequest;
import com.ikhsan.securepaywallet.transaction.dto.req.TransferRequest;
import com.ikhsan.securepaywallet.transaction.dto.res.TransactionResponse;
import com.ikhsan.securepaywallet.transaction.entity.TransactionEntity;
import com.ikhsan.securepaywallet.transaction.repo.TransactionRepository;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.service.IUser;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletBalanceResponse;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletTransferResponse;
import com.ikhsan.securepaywallet.wallet.service.IWallet;

@Service
public class TransactionService implements ITransaction {

    private final TransactionRepository transactionRepository;
    private final ILedger ledger;
    private final IWallet walletService;
    private final IUser userService;
    private final BigDecimal maxTopupAmount;
    private final BigDecimal transferFee;

    public TransactionService(TransactionRepository transactionRepository,
                              ILedger ledger,
                              IWallet walletService,
                              IUser userService,
                              @Value("${transaction.topup.max-amount:10000000}") BigDecimal maxTopupAmount,
                              @Value("${transaction.transfer.fee:0}") BigDecimal transferFee) {
        this.transactionRepository = transactionRepository;
        this.ledger = ledger;
        this.walletService = walletService;
        this.userService = userService;
        this.maxTopupAmount = maxTopupAmount;
        this.transferFee = transferFee;
    }

    @Override
    @Transactional
    public TransactionResponse topUp(UUID userId, TopupRequest request) {
        BigDecimal amount = requireValidTopupAmount(request);

        WalletBalanceResponse wallet = walletService.increaseBalanceWithLock(userId, amount);

        TransactionEntity saved = transactionRepository.saveAndFlush(
                buildTransaction(amount, BigDecimal.ZERO, request.getDescription(), TransactionType.TOPUP));

        ledger.post(saved.getId(), List.of(
                new EntryLine(ledger.systemAccountId(LedgerAccountType.SYSTEM_CASH), EntryDirection.DEBIT, amount),
                new EntryLine(ledger.walletAccountId(wallet.getWalletId()), EntryDirection.CREDIT, amount)));

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TransactionResponse transfer(UUID senderUserId, TransferRequest request) {
        BigDecimal amount = requireValidTransferAmount(request);

        UserResponse receiver = userService.getUserByUsername(request.getReceiverUsername());
        if (receiver.getId().equals(senderUserId)) {
            throw new IllegalArgumentException("tidak bisa transfer ke wallet sendiri");
        }

        WalletTransferResponse wallets = walletService.lockAndTransfer(senderUserId, receiver.getId(), amount,
                transferFee);

        TransactionEntity saved = transactionRepository.saveAndFlush(
                buildTransaction(amount, transferFee, request.getDescription(), TransactionType.TRANSFER));

        List<EntryLine> lines = new ArrayList<>();
        lines.add(new EntryLine(ledger.walletAccountId(wallets.getSenderWalletId()), EntryDirection.DEBIT,
                amount.add(transferFee)));
        lines.add(new EntryLine(ledger.walletAccountId(wallets.getReceiverWalletId()), EntryDirection.CREDIT, amount));
        if (transferFee.signum() > 0) {
            lines.add(new EntryLine(ledger.systemAccountId(LedgerAccountType.SYSTEM_FEE), EntryDirection.CREDIT,
                    transferFee));
        }

        ledger.post(saved.getId(), lines);

        return toResponse(saved);
    }

    private BigDecimal requireValidTopupAmount(TopupRequest request) {
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

    private BigDecimal requireValidTransferAmount(TransferRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("amount wajib diisi");
        }

        BigDecimal amount = request.getAmount();
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount harus lebih besar dari 0, diterima: " + amount);
        }
        return amount;
    }

    private TransactionEntity buildTransaction(BigDecimal amount, BigDecimal fee, String description,
                                               TransactionType type) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setReferenceNumber(type.name() + "-" + UUID.randomUUID());
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(amount);
        transaction.setFee(fee);
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
