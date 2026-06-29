package com.ikhsan.securepaywallet.mutations;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MutationRepository extends JpaRepository<MutationsEntity, UUID> {

}
