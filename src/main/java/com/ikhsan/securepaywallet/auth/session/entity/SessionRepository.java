package com.ikhsan.securepaywallet.auth.session.entity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

}
