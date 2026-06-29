package com.ikhsan.securepaywallet.wallet;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {

}
