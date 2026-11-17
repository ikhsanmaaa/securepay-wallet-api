package com.ikhsan.securepaywallet.wallet.dto.res;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
