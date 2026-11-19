package com.ikhsan.securepaywallet.wallet.dto.res;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletTransferResponse {
    private UUID senderWalletId;
    private UUID receiverWalletId;
}
