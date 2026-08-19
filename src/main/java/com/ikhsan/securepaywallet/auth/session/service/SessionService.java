package com.ikhsan.securepaywallet.auth.session.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikhsan.securepaywallet.auth.session.entity.SessionEntity;
import com.ikhsan.securepaywallet.auth.session.entity.SessionRepository;
import com.ikhsan.securepaywallet.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService implements ISession {

    private static final Duration IDLE_TIMEOUT = Duration.ofMinutes(30);
    private static final Duration ABSOLUTE_TIMEOUT = Duration.ofHours(1);

    private final SessionRepository sessionRepository;
    private final Clock clock;

    @Transactional
    public SessionEntity createSession(UserEntity user) {
        Instant now = Instant.now(clock);

        SessionEntity session = new SessionEntity();

        session.setUser(user);
        session.setCreatedAt(now);
        session.setLastActivityAt(now);
        session.setExpiresAt(now.plus(ABSOLUTE_TIMEOUT));

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public boolean isSessionValid(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .map(this::isSessionValid)
                .orElse(false);
    }

    private boolean isSessionValid(SessionEntity session) {
        Instant now = Instant.now(clock);

        if (session.getRevokedAt() != null) {
            return false;
        }

        if (!now.isBefore(session.getExpiresAt())) {
            return false;
        }

        Instant idleExpiresAt = session.getLastActivityAt().plus(IDLE_TIMEOUT);

        if (!now.isBefore(idleExpiresAt)) {
            return false;
        }

        return true;
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getRevokedAt() != null) {
            return;
        }

        session.setRevokedAt(Instant.now(clock));
        sessionRepository.save(session);
    }

    @Transactional
    public void updateActivity(UUID sessionId) {

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getRevokedAt() != null) {
            return;
        }

        session.setLastActivityAt(Instant.now(clock));

        sessionRepository.save(session);
    }

}