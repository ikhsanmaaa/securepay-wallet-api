package com.ikhsan.securepaywallet.transaction.entity;

import java.math.BigDecimal;

import com.ikhsan.securepaywallet.common.baseclass.BaseEntity;
import com.ikhsan.securepaywallet.enumerate.TransactionStatus;
import com.ikhsan.securepaywallet.enumerate.TransactionType;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @ManyToOne
    @JoinColumn(name = "sender_wallet_id")
    private WalletEntity senderWallet;

    @ManyToOne
    @JoinColumn(name = "receiver_wallet_id")
    private WalletEntity receiverWallet;

    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(columnDefinition = "TEXT")
    private String description;

}
