package com.ikhsan.securepaywallet.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.auth.dto.req.LoginRequest;
import com.ikhsan.securepaywallet.auth.dto.res.TokenResponse;
import com.ikhsan.securepaywallet.auth.security.JwtService;
import com.ikhsan.securepaywallet.auth.session.entity.SessionEntity;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.enumerate.Role;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private SessionService sessionService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                sessionService);
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("john");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);

        SessionEntity session = new SessionEntity();
        session.setId(sessionId);
        session.setUser(user);

        when(userRepository.findFirstByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encoded-password")).thenReturn(true);

        when(sessionService.createSession(user))
                .thenReturn(session);

        when(jwtService.generateAccessToken(
                userId,
                "USER",
                sessionId)).thenReturn("access-token");

        // Act
        TokenResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(
                "access-token",
                response.getToken());

        verify(sessionService)
                .createSession(user);

        verify(jwtService)
                .generateAccessToken(
                        userId,
                        "USER",
                        sessionId);
    }

    @Test
    void login_shouldNotCreateSession_whenUsernameDoesNotExist() {

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password123");

        when(userRepository.findFirstByUsername("unknown"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request));

        verify(sessionService, never())
                .createSession(any());
    }

    @Test
    void login_shouldNotCreateSession_whenPasswordIsInvalid() {

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrong-password");

        UserEntity user = new UserEntity();
        user.setUsername("john");
        user.setPassword("encoded-password");

        when(userRepository.findFirstByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "encoded-password")).thenReturn(false);

        // Act & Assert
        assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request));

        verify(sessionService, never())
                .createSession(any());
    }

    @Test
    void logout_shouldRevokeSession() {

        // Arrange
        UUID sessionId = UUID.randomUUID();

        // Act
        authService.logout(sessionId);

        // Assert
        verify(sessionService)
                .revokeSession(sessionId);
    }
}