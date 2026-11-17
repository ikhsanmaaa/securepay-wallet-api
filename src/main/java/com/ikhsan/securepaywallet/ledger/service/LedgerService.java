package com.ikhsan.securepaywallet.ledger.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.enumerate.EntryDirection;
import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;
import com.ikhsan.securepaywallet.ledger.dto.req.EntryLine;
import com.ikhsan.securepaywallet.ledger.entity.LedgerAccountEntity;
import com.ikhsan.securepaywallet.ledger.entity.LedgerEntryEntity;
import com.ikhsan.securepaywallet.ledger.repo.LedgerAccountRepository;
import com.ikhsan.securepaywallet.ledger.repo.LedgerEntryRepository;

@Service
public class LedgerService implements ILedger {

    private static final int MAX_SCALE = 2;
    private static final String WALLET_ACCOUNT_PREFIX = "W-";

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerAccountRepository ledgerAccountRepository,
                         LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void createWalletAccount(UUID walletId) {
        if (walletId == null) {
            throw new IllegalArgumentException("walletId wajib diisi");
        }

        ledgerAccountRepository.save(
                new LedgerAccountEntity(WALLET_ACCOUNT_PREFIX + walletId, LedgerAccountType.USER_WALLET, walletId));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void post(UUID transactionId, List<EntryLine> lines) {
        requirePost(transactionId, lines);

        Map<UUID, LedgerAccountEntity> accounts = resolveAccounts(lines);

        ledgerEntryRepository.saveAll(lines.stream()
                .map(line -> LedgerEntryEntity.builder()
                        .transactionId(transactionId)
                        .account(accounts.get(line.accountId()))
                        .direction(line.direction())
                        .amount(line.amount())
                        .build())
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UUID walletAccountId(UUID walletId) {
        LedgerAccountEntity account = ledgerAccountRepository.findByWalletId(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "akun ledger tidak ditemukan untuk wallet: " + walletId));
        return account.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public UUID systemAccountId(LedgerAccountType type) {
        if (type == null || type == LedgerAccountType.USER_WALLET) {
            throw new IllegalArgumentException("systemAccountId hanya untuk akun sistem, diterima: " + type);
        }

        LedgerAccountEntity account = ledgerAccountRepository.findByCode(type.name())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "akun sistem belum di-seed di ledger: " + type));
        return account.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal balanceOf(UUID walletId) {
        if (walletId == null) {
            throw new IllegalArgumentException("walletId wajib diisi");
        }

        LedgerAccountEntity account = ledgerAccountRepository.findByWalletId(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "akun ledger tidak ditemukan untuk wallet: " + walletId));

        BigDecimal balance = ledgerEntryRepository.sumBalanceByAccountId(account.getId(), EntryDirection.CREDIT);
        return balance == null ? BigDecimal.ZERO : balance;
    }

    private void requirePost(UUID transactionId, List<EntryLine> lines) {
        if (transactionId == null) {
            throw new IllegalArgumentException("transactionId wajib diisi");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("entri ledger tidak boleh kosong");
        }

        for (EntryLine line : lines) {
            if (line == null || line.accountId() == null) {
                throw new IllegalArgumentException("accountId wajib diisi");
            }
            if (line.direction() == null) {
                throw new IllegalArgumentException("direction wajib diisi");
            }
            BigDecimal amount = line.amount();
            if (amount == null || amount.signum() <= 0) {
                throw new IllegalArgumentException("amount harus lebih besar dari 0, diterima: " + amount);
            }
            if (amount.scale() > MAX_SCALE) {
                throw new IllegalArgumentException("amount maksimal " + MAX_SCALE + " desimal, diterima: " + amount);
            }
        }

        BigDecimal totalDebit = total(lines, EntryDirection.DEBIT);
        BigDecimal totalCredit = total(lines, EntryDirection.CREDIT);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException(
                    "entri ledger tidak seimbang: debit=" + totalDebit + " kredit=" + totalCredit);
        }
    }

    private Map<UUID, LedgerAccountEntity> resolveAccounts(List<EntryLine> lines) {
        Set<UUID> accountIds = lines.stream().map(EntryLine::accountId).collect(Collectors.toSet());

        Map<UUID, LedgerAccountEntity> accounts = ledgerAccountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(LedgerAccountEntity::getId, Function.identity()));

        if (accounts.size() != accountIds.size()) {
            Set<UUID> missing = new HashSet<>(accountIds);
            missing.removeAll(accounts.keySet());
            throw new IllegalArgumentException("akun ledger tidak ditemukan: " + missing);
        }
        return accounts;
    }

    private BigDecimal total(List<EntryLine> lines, EntryDirection direction) {
        return lines.stream()
                .filter(line -> line.direction() == direction)
                .map(EntryLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
