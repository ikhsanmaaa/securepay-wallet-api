package com.ikhsan.securepaywallet.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ikhsan.securepaywallet.auth.security.JwtAuthenticationFilter;
import com.ikhsan.securepaywallet.auth.security.JwtService;
import com.ikhsan.securepaywallet.auth.service.AuthService;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.common.config.SecurityConfig;

@ActiveProfiles("test")
@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {
        AuthController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
@TestPropertySource(properties = {
        "jwt.secret=hwDe+1mWsxCXpK48PDrwlXCF2ioFhbSpmxcmWTvZbR0=",
        "jwt.access-token-expiration=900000"
})
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void logout_shouldReturnNoContent_whenAuthenticated()
            throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(
                userId,
                "USER",
                sessionId);

        when(sessionService.isSessionValid(sessionId))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/logout")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(authService).logout(sessionId);
    }

    @Test
    void logout_shouldReturnUnauthorized_whenTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/logout")).andExpect(status().isUnauthorized());
    }
}