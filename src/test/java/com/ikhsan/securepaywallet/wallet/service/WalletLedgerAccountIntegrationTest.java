package com.ikhsan.securepaywallet.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;
import com.ikhsan.securepaywallet.ledger.entity.LedgerAccountEntity;
import com.ikhsan.securepaywallet.support.PostgresTestBase;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;

class WalletLedgerAccountIntegrationTest extends PostgresTestBase {

    @Test
    @Transactional
    @DisplayName("Wallet baru mendapat tepat satu akun ledger USER_WALLET")
    void createWallet_shouldCreateLedgerAccountLinkedToWallet() {
        WalletEntity wallet = walletService.createWallet(newUser());

        LedgerAccountEntity account = ledgerAccountRepository.findByWalletId(wallet.getId()).orElseThrow();
        assertEquals(LedgerAccountType.USER_WALLET, account.getType());
        assertEquals("W-" + wallet.getId(), account.getCode());
        assertNotNull(account.getId());
    }

    @Test
    @Transactional
    void createWallet_shouldNotCreateSecondLedgerAccount_whenWalletExists() {
        var user = newUser();

        WalletEntity first = walletService.createWallet(user);
        LedgerAccountEntity firstAccount = ledgerAccountRepository.findByWalletId(first.getId()).orElseThrow();

        WalletEntity second = walletService.createWallet(user);

        assertEquals(first.getId(), second.getId());
        assertEquals(firstAccount.getId(),
                ledgerAccountRepository.findByWalletId(second.getId()).orElseThrow().getId());
    }
}
