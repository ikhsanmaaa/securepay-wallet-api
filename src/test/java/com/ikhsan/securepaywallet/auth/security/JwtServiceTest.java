package com.ikhsan.securepaywallet.auth.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "vxAutHKUypyC/XMvDlqWCteVahXdBCsgjy2aJp1u9Iw=";

    private static final long EXPIRATION = 900000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                SECRET,
                EXPIRATION);
    }

    @Test
    void generateAccessToken_shouldGenerateValidToken() {

        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "USER";
        UUID sessionId = UUID.randomUUID();

        // Act
        String token = jwtService.generateAccessToken(
                userId,
                role,
                sessionId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractSubject_shouldReturnUserId() {

        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "USER";
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(
                userId,
                role,
                sessionId);

        // Act
        String subject = jwtService.extractSubject(token);

        // Assert
        assertEquals(
                userId.toString(),
                subject);
    }

    @Test
    void extractRole_shouldReturnRole() {

        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "USER";
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(
                userId,
                role,
                sessionId);

        // Act
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertEquals(
                role,
                extractedRole);
    }

    @Test
    void extractSessionId_shouldReturnSessionId() {

        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "USER";
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(
                userId,
                role,
                sessionId);

        // Act
        String extractedSessionId = jwtService.extractSessionId(token);

        // Assert
        assertEquals(
                sessionId.toString(),
                extractedSessionId);
    }

    @Test
    void isValid_shouldReturnTrueForValidToken() {

        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "USER";
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(
                userId,
                role,
                sessionId);

        // Act
        boolean valid = jwtService.isValid(token);

        // Assert
        assertTrue(valid);
    }

    @Test
    void isValid_shouldReturnFalseForInvalidToken() {

        // Arrange
        String invalidToken = "this.is.not.a.valid.jwt";

        // Act
        boolean valid = jwtService.isValid(invalidToken);

        // Assert
        assertFalse(valid);
    }
}