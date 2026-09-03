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

        private static final Instant FIXED_NOW = Instant.parse("2026-08-16T06:00:00Z");

        private static final long IDLE_TIMEOUT_SECONDS = 30 * 60;

        private static final long ABSOLUTE_TIMEOUT_SECONDS = 60 * 60;

        @Mock
        private SessionRepository sessionRepository;

        private Clock clock;

        private SessionService sessionService;

        @BeforeEach
        void setUp() {

                clock = Clock.fixed(
                                FIXED_NOW,
                                ZoneOffset.UTC);

                sessionService = new SessionService(
                                sessionRepository,
                                clock);
        }

        // =========================
        // CREATE SESSION
        // =========================

        @Test
        void createSession_shouldCreateSessionWithCorrectTimestamps() {

                // Arrange
                UserEntity user = new UserEntity();

                when(sessionRepository.save(any(SessionEntity.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                SessionEntity result = sessionService.createSession(user);

                // Assert
                assertNotNull(result);

                assertEquals(
                                user,
                                result.getUser());

                assertEquals(
                                FIXED_NOW,
                                result.getCreatedAt());

                assertEquals(
                                FIXED_NOW,
                                result.getLastActivityAt());

                assertEquals(
                                FIXED_NOW.plusSeconds(
                                                ABSOLUTE_TIMEOUT_SECONDS),
                                result.getExpiresAt());

                assertNull(
                                result.getRevokedAt());

                verify(sessionRepository)
                                .save(any(SessionEntity.class));
        }

        // =========================
        // SESSION VALIDATION
        // =========================

        @Test
        void isSessionValid_shouldReturnTrue_whenSessionIsActive() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(10 * 60),
                                FIXED_NOW.minusSeconds(5 * 60),
                                FIXED_NOW.plusSeconds(50 * 60));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertTrue(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenSessionDoesNotExist() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.empty());

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenSessionIsRevoked() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(10 * 60),
                                FIXED_NOW.minusSeconds(5 * 60),
                                FIXED_NOW.plusSeconds(50 * 60));

                session.setRevokedAt(
                                FIXED_NOW.minusSeconds(60));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertFalse(result);
        }

        // =========================
        // ABSOLUTE TIMEOUT
        // =========================

        @Test
        void isSessionValid_shouldReturnFalse_whenAbsoluteTimeoutIsReached() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(
                                                ABSOLUTE_TIMEOUT_SECONDS),
                                FIXED_NOW.minusSeconds(60),
                                FIXED_NOW);

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenAbsoluteTimeoutHasPassed() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(
                                                ABSOLUTE_TIMEOUT_SECONDS + 1),
                                FIXED_NOW.minusSeconds(60),
                                FIXED_NOW.minusSeconds(1));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnTrue_justBeforeAbsoluteTimeout() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(
                                                ABSOLUTE_TIMEOUT_SECONDS - 1),
                                FIXED_NOW.minusSeconds(60),
                                FIXED_NOW.plusSeconds(1));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertTrue(result);
        }

        // =========================
        // IDLE TIMEOUT
        // =========================

        @Test
        void isSessionValid_shouldReturnFalse_whenIdleTimeoutIsReached() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(60 * 60),
                                FIXED_NOW.minusSeconds(
                                                IDLE_TIMEOUT_SECONDS),
                                FIXED_NOW.plusSeconds(60 * 60));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnFalse_whenIdleTimeoutHasPassed() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(60 * 60),
                                FIXED_NOW.minusSeconds(
                                                IDLE_TIMEOUT_SECONDS + 1),
                                FIXED_NOW.plusSeconds(60 * 60));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertFalse(result);
        }

        @Test
        void isSessionValid_shouldReturnTrue_justBeforeIdleTimeout() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                SessionEntity session = createSession(
                                FIXED_NOW.minusSeconds(60 * 60),
                                FIXED_NOW.minusSeconds(
                                                IDLE_TIMEOUT_SECONDS - 1),
                                FIXED_NOW.plusSeconds(60 * 60));

                when(sessionRepository.findById(sessionId))
                                .thenReturn(Optional.of(session));

                // Act
                boolean result = sessionService.isSessionValid(sessionId);

                // Assert
                assertTrue(result);
        }

        // =========================
        // REVOKE SESSION
        // =========================

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
                assertEquals(
                                FIXED_NOW,
                                session.getRevokedAt());

                verify(sessionRepository)
                                .save(session);
        }

        @Test
        void revokeSession_shouldDoNothing_whenSessionAlreadyRevoked() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                Instant revokedAt = FIXED_NOW.minusSeconds(60);

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

        // =========================
        // UPDATE ACTIVITY
        // =========================

        @Test
        void updateActivity_shouldUpdateLastActivityAt() {

                // Arrange
                UUID sessionId = UUID.randomUUID();

                Instant oldActivityAt = FIXED_NOW.minusSeconds(10 * 60);

                Instant absoluteExpiration = FIXED_NOW.plusSeconds(60 * 60);

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
                                FIXED_NOW,
                                session.getLastActivityAt());

                assertNotEquals(
                                oldActivityAt,
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

                Instant revokedAt = FIXED_NOW.minusSeconds(30 * 60);

                Instant oldActivityAt = FIXED_NOW.minusSeconds(60 * 60);

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

        // =========================
        // HELPER
        // =========================

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
}