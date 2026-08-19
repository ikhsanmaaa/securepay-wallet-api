package com.ikhsan.securepaywallet.auth.session.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ikhsan.securepaywallet.auth.session.entity.SessionEntity;
import com.ikhsan.securepaywallet.auth.session.entity.SessionRepository;
import com.ikhsan.securepaywallet.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

        @Mock
        private SessionRepository sessionRepository;

        private Clock clock;

        private SessionService sessionService;

        private final Instant fixedNow = Instant.parse("2026-08-16T06:00:00Z");

        @BeforeEach
        void setUp() {
                clock = Clock.fixed(
                                fixedNow,
                                ZoneOffset.UTC);

                sessionService = new SessionService(
                                sessionRepository,
                                clock);
        }

        @Test
        void createSession_shouldCreateSessionWithCorrectTimestamps() {
                UserEntity user = new UserEntity();

                SessionEntity savedSession = new SessionEntity();
                savedSession.setUser(user);
                savedSession.setCreatedAt(fixedNow);
                savedSession.setLastActivityAt(fixedNow);
                savedSession.setExpiresAt(
                                fixedNow.plusSeconds(60 * 60));

                when(sessionRepository.save(any(SessionEntity.class)))
                                .thenReturn(savedSession);

                SessionEntity result = sessionService.createSession(user);

                assertNotNull(result);
                assertEquals(user, result.getUser());
                assertEquals(fixedNow, result.getCreatedAt());
                assertEquals(fixedNow, result.getLastActivityAt());
                assertEquals(
                                fixedNow.plusSeconds(60 * 60),
                                result.getExpiresAt());
                assertNull(result.getRevokedAt());

                verify(sessionRepository).save(any(SessionEntity.class));
        }

        @Test
        void isSessionValid_shouldReturnTrue_whenSessionIsActive() {
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                fixedNow,
                                fixedNow,
                                fixedNow.plusSeconds(60 * 60));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                boolean result = sessionService.isSessionValid(sessionId);

                assertTrue(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenSessionIsRevoked() {
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                fixedNow,
                                fixedNow,
                                fixedNow.plusSeconds(60 * 60));

                session.setRevokedAt(fixedNow);

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                boolean result = sessionService.isSessionValid(sessionId);

                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenAbsoluteTimeoutIsReached() {
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                fixedNow.minusSeconds(60 * 60),
                                fixedNow.minusSeconds(60),
                                fixedNow);

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                boolean result = sessionService.isSessionValid(sessionId);

                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenIdleTimeoutIsReached() {
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                fixedNow.minusSeconds(60 * 60),
                                fixedNow.minusSeconds(30 * 60),
                                fixedNow.plusSeconds(60 * 60));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                boolean result = sessionService.isSessionValid(sessionId);

                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenSessionDoesNotExist() {
                UUID sessionId = UUID.randomUUID();

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.empty());

                boolean result = sessionService.isSessionValid(sessionId);

                assertFalse(result);
        }

        private SessionEntity createSession(
                        Instant createdAt,
                        Instant lastActivityAt,
                        Instant expiresAt) {
                SessionEntity session = new SessionEntity();

                session.setCreatedAt(createdAt);
                session.setLastActivityAt(lastActivityAt);
                session.setExpiresAt(expiresAt);

                return session;
        }

        @Test
        void revokeSession_shouldSetRevokedAt_whenSessionIsActive() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = new SessionEntity();
                session.setRevokedAt(null);

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                sessionService.revokeSession(sessionId);

                // Assert
                assertNotNull(session.getRevokedAt());

                verify(sessionRepository).save(session);
        }

        @Test
        void revokeSession_shouldDoNothing_whenSessionAlreadyRevoked() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                Instant revokedAt = Instant.parse(
                                "2026-08-16T06:00:00Z");

                SessionEntity session = new SessionEntity();
                session.setRevokedAt(revokedAt);

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                sessionService.revokeSession(sessionId);

                // Assert
                assertEquals(
                                revokedAt,
                                session.getRevokedAt());

                verify(sessionRepository, never())
                                .save(any(SessionEntity.class));
        }

        @Test
        void revokeSession_shouldThrowException_whenSessionDoesNotExist() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(
                                IllegalArgumentException.class,
                                () -> sessionService.revokeSession(sessionId));

                verify(sessionRepository, never())
                                .save(any(SessionEntity.class));
        }

        @Test
        void updateActivity_shouldUpdateLastActivityAt() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                Instant oldActivityAt = Instant.parse("2026-08-16T06:00:00Z");

                Instant absoluteExpiration = Instant.parse("2026-08-16T07:00:00Z");

                SessionEntity session = new SessionEntity();

                session.setLastActivityAt(oldActivityAt);
                session.setExpiresAt(absoluteExpiration);
                session.setRevokedAt(null);

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                sessionService.updateActivity(sessionId);

                // Assert
                assertEquals(
                                fixedNow,
                                session.getLastActivityAt());

                assertEquals(
                                absoluteExpiration,
                                session.getExpiresAt());

                verify(sessionRepository)
                                .save(session);
        }

        @Test
        void updateActivity_shouldDoNothing_whenSessionIsRevoked() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                Instant revokedAt = Instant.parse("2026-08-16T05:30:00Z");

                Instant oldActivityAt = Instant.parse("2026-08-16T05:00:00Z");

                SessionEntity session = new SessionEntity();

                session.setLastActivityAt(oldActivityAt);
                session.setRevokedAt(revokedAt);

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                sessionService.updateActivity(sessionId);

                // Assert
                assertEquals(
                                oldActivityAt,
                                session.getLastActivityAt());

                verify(sessionRepository, never())
                                .save(any(SessionEntity.class));
        }

        @Test
        void updateActivity_shouldThrowException_whenSessionDoesNotExist() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(
                                IllegalArgumentException.class,
                                () -> sessionService.updateActivity(sessionId));

                verify(sessionRepository, never())
                                .save(any(SessionEntity.class));
        }

}