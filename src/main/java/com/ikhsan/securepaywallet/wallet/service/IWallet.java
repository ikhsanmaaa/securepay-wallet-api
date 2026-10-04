package com.ikhsan.securepaywallet.wallet.service;

import java.util.UUID;

import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletResponse;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;

public interface IWallet {

    WalletEntity createWallet(UserEntity user);

    WalletResponse getWalletByUserId(UUID userId);
}
