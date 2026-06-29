package com.ikhsan.securepaywallet.wallet;

import java.util.Set;
import java.util.UUID;

import com.ikhsan.securepaywallet.common.BaseEntity;
import com.ikhsan.securepaywallet.enumerate.WalletStatus;
import com.ikhsan.securepaywallet.user.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "wallet")
@NoArgsConstructor
public class WalletEntity extends BaseEntity {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany
    @JoinColumn(name = "id", nullable = false)
    private UserEntity user_id;

    private Integer balance;

    @Enumerated(EnumType.STRING)
    private Set<WalletStatus> status;
}
