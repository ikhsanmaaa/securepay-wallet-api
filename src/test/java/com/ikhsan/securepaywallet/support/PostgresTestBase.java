package com.ikhsan.securepaywallet.support;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;
import com.ikhsan.securepaywallet.enumerate.Role;
import com.ikhsan.securepaywallet.enumerate.TransactionStatus;
import com.ikhsan.securepaywallet.enumerate.TransactionType;
import com.ikhsan.securepaywallet.ledger.entity.LedgerAccountEntity;
import com.ikhsan.securepaywallet.ledger.repo.LedgerAccountRepository;
import com.ikhsan.securepaywallet.transaction.entity.TransactionEntity;
import com.ikhsan.securepaywallet.transaction.repo.TransactionRepository;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;
import com.ikhsan.securepaywallet.wallet.service.IWallet;

@SpringBootTest
@Import(EmbeddedPostgresTestConfig.class)
public abstract class PostgresTestBase {

    @Autowired
    protected IWallet walletService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TransactionRepository transactionRepository;

    @Autowired
    protected LedgerAccountRepository ledgerAccountRepository;

    protected UserEntity newUser() {
        String suffix = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        user.setUsername("it-" + suffix);
        user.setName("Integration Test User");
        user.setEmail(suffix + "@securepay.test");
        user.setPassword("hashed");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    protected UserEntity newUserWithWallet() {
        UserEntity user = newUser();
        walletService.createWallet(user);
        return user;
    }

    protected WalletEntity newWallet() {
        return walletService.createWallet(newUser());
    }

    protected LedgerAccountEntity newWalletAccount() {
        WalletEntity wallet = newWallet();
        return ledgerAccountRepository.findByWalletId(wallet.getId()).orElseThrow();
    }

    protected LedgerAccountEntity systemAccount(LedgerAccountType type) {
        return ledgerAccountRepository.findByCode(type.name()).orElseThrow();
    }

    protected UUID newTransactionId(BigDecimal amount) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setReferenceNumber("TEST-" + UUID.randomUUID());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(amount);
        transaction.setFee(BigDecimal.ZERO);
        return transactionRepository.saveAndFlush(transaction).getId();
    }
}
