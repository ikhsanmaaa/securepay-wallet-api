package com.ikhsan.securepaywallet.ledger.dto.req;

import java.math.BigDecimal;
import java.util.UUID;

import com.ikhsan.securepaywallet.enumerate.EntryDirection;

public record EntryLine(UUID accountId, EntryDirection direction, BigDecimal amount) {
}
