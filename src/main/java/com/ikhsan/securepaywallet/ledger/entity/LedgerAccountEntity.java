package com.ikhsan.securepaywallet.ledger.entity;

import java.util.UUID;

import com.ikhsan.securepaywallet.common.baseclass.BaseEntity;
import com.ikhsan.securepaywallet.enumerate.LedgerAccountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ledger_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LedgerAccountEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LedgerAccountType type;

    @Column(name = "wallet_id", unique = true)
    private UUID walletId;
}
