package com.ikhsan.securepaywallet.wallet.dto.res;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletBalanceResponse {
    private UUID walletId;
    private BigDecimal balance;
}
