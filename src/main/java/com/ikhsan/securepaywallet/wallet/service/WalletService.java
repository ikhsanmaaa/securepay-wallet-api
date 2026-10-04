package com.ikhsan.securepaywallet.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.enumerate.WalletStatus;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletResponse;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;
import com.ikhsan.securepaywallet.wallet.repo.WalletRepository;

@Service
public class WalletService implements IWallet {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
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
                    return walletRepository.save(wallet);
                });
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
