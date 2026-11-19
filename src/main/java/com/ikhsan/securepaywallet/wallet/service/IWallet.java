package com.ikhsan.securepaywallet.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletBalanceResponse;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletResponse;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletTransferResponse;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;

public interface IWallet {

    WalletEntity createWallet(UserEntity user);

    WalletResponse getWalletByUserId(UUID userId);

    WalletBalanceResponse increaseBalanceWithLock(UUID userId, BigDecimal amount);

    WalletTransferResponse lockAndTransfer(UUID senderUserId, UUID receiverUserId, BigDecimal amount, BigDecimal fee);
}
