package com.ikhsan.securepaywallet.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.enumerate.WalletStatus;
import com.ikhsan.securepaywallet.ledger.service.ILedger;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletBalanceResponse;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletResponse;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletTransferResponse;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;
import com.ikhsan.securepaywallet.wallet.repo.WalletRepository;

import jakarta.persistence.EntityManager;

@Service
public class WalletService implements IWallet {

    private final WalletRepository walletRepository;
    private final ILedger ledger;
    private final EntityManager entityManager;

    public WalletService(WalletRepository walletRepository, ILedger ledger, EntityManager entityManager) {
        this.walletRepository = walletRepository;
        this.ledger = ledger;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public WalletEntity createWallet(UserEntity user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    WalletEntity wallet = new WalletEntity();
                    wallet.setUser(user);
                    wallet.setBalance(BigDecimal.ZERO);
                    wallet.setStatus(WalletStatus.ACTIVE);
                    WalletEntity saved = walletRepository.saveAndFlush(wallet);
                    ledger.createWalletAccount(saved.getId());
                    return saved;
                });
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public WalletBalanceResponse increaseBalanceWithLock(UUID userId, BigDecimal amount) {
        if (userId == null || amount == null) {
            throw new IllegalArgumentException("userId dan amount wajib diisi");
        }

        WalletEntity wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "wallet not found for this id: " + userId));
        entityManager.refresh(wallet);

        requireActive(wallet);

        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);

        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .balance(newBalance)
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public WalletTransferResponse lockAndTransfer(UUID senderUserId, UUID receiverUserId, BigDecimal amount,
                                                  BigDecimal fee) {
        if (senderUserId == null || receiverUserId == null || amount == null || fee == null) {
            throw new IllegalArgumentException("senderUserId, receiverUserId, amount, dan fee wajib diisi");
        }

        UUID senderWalletId = walletIdOf(senderUserId);
        UUID receiverWalletId = walletIdOf(receiverUserId);
        if (senderWalletId.equals(receiverWalletId)) {
            throw new IllegalArgumentException("wallet pengirim dan penerima tidak boleh sama");
        }

        boolean senderIsFirst = senderWalletId.compareTo(receiverWalletId) < 0;
        WalletEntity firstWallet = lockWallet(senderIsFirst ? senderWalletId : receiverWalletId);
        WalletEntity secondWallet = lockWallet(senderIsFirst ? receiverWalletId : senderWalletId);

        WalletEntity sender = senderIsFirst ? firstWallet : secondWallet;
        WalletEntity receiver = senderIsFirst ? secondWallet : firstWallet;

        requireActive(sender);
        requireActive(receiver);

        BigDecimal totalOut = amount.add(fee);
        if (sender.getBalance().compareTo(totalOut) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "saldo tidak cukup: " + sender.getBalance() + " < " + totalOut);
        }

        sender.setBalance(sender.getBalance().subtract(totalOut));
        receiver.setBalance(receiver.getBalance().add(amount));

        return WalletTransferResponse.builder()
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .build();
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(UUID userId) {

        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "wallet not found for this id: " + userId));

        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(userId)
                .balance(wallet.getBalance())
                .status(wallet.getStatus().toString())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private WalletEntity lockWallet(UUID walletId) {
        WalletEntity wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "wallet not found for this id: " + walletId));
        entityManager.refresh(wallet);
        return wallet;
    }

    private UUID walletIdOf(UUID userId) {
        return walletRepository.findWalletIdByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "wallet not found for this id: " + userId));
    }

    private void requireActive(WalletEntity wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "wallet tidak aktif: " + wallet.getStatus());
        }
    }
}
