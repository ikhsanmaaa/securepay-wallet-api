package com.ikhsan.securepaywallet.transaction.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ikhsan.securepaywallet.transaction.entity.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

}
