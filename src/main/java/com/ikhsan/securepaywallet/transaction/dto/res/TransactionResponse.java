package com.ikhsan.securepaywallet.transaction.dto.res;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionResponse {
    private UUID id;
    private String referenceNumber;
    private String type;
    private String status;
    private BigDecimal amount;
    private BigDecimal fee;
    private Instant createdAt;
}
