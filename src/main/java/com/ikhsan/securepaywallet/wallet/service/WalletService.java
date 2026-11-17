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
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;
import com.ikhsan.securepaywallet.wallet.repo.WalletRepository;

@Service
public class WalletService implements IWallet {

    private final WalletRepository walletRepository;
    private final ILedger ledger;

    public WalletService(WalletRepository walletRepository, ILedger ledger) {
        this.walletRepository = walletRepository;
        this.ledger = ledger;
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

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "wallet tidak aktif: " + wallet.getStatus());
        }

        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);

        return WalletBalanceResponse.builder()
                .walletId(wallet.getId())
                .balance(newBalance)
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
}
