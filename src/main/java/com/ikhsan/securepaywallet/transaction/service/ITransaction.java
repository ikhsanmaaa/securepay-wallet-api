package com.ikhsan.securepaywallet.transaction.service;

import java.util.UUID;

import com.ikhsan.securepaywallet.transaction.dto.req.TopupRequest;
import com.ikhsan.securepaywallet.transaction.dto.req.TransferRequest;
import com.ikhsan.securepaywallet.transaction.dto.res.TransactionResponse;

public interface ITransaction {

    TransactionResponse topUp(UUID userId, TopupRequest request);

    TransactionResponse transfer(UUID userId, TransferRequest request);
}
