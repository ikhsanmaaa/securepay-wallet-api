package com.ikhsan.securepaywallet.mutations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import com.ikhsan.securepaywallet.transaction.TransactionEntity;
import com.ikhsan.securepaywallet.wallet.WalletEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mutations")
public class MutationsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "id")
    private WalletEntity wallet_id;

    @OneToOne
    @JoinColumn(name = "id")
    private TransactionEntity transaction_id;

    @Column(nullable = false)
    private BigDecimal balance_before;

    @Column(nullable = false)
    private BigDecimal balance_after;

    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    protected Instant createdAt;

}
