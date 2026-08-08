package com.ikhsan.securepaywallet.transaction;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import com.ikhsan.securepaywallet.common.BaseEntity;
import com.ikhsan.securepaywallet.enumerate.TransactionType;
import com.ikhsan.securepaywallet.wallet.WalletEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "transaction")
public class TransactionEntity extends BaseEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne()
    @JoinColumn(name = "sender_wallet_id")
    private WalletEntity sender_wallet_id;

    @ManyToOne()
    @JoinColumn(name = "receiver_wallet_id")
    private WalletEntity receiver_wallet_id;

    private Integer reference_number;

    @Enumerated(EnumType.STRING)
    private Set<TransactionType> type;

    private BigDecimal amount;

    private BigDecimal fee;

    private String description;

}
