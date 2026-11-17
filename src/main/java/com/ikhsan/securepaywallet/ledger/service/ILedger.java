package com.ikhsan.securepaywallet.ledger.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;
import com.ikhsan.securepaywallet.ledger.dto.req.EntryLine;

public interface ILedger {

    void createWalletAccount(UUID walletId);

    UUID walletAccountId(UUID walletId);

    UUID systemAccountId(LedgerAccountType type);

    void post(UUID transactionId, List<EntryLine> lines);

    BigDecimal balanceOf(UUID walletId);
}
