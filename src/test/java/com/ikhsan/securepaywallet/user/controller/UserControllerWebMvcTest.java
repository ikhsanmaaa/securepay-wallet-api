package com.ikhsan.securepaywallet.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.common.config.SecurityConfig;
import com.ikhsan.securepaywallet.user.service.UserService;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = {
                UserController.class,
                SecurityConfig.class,
                JwtAuthenticationFilter.class,
                JwtService.class
})
@TestPropertySource(properties = {
                "jwt.secret=hwDe+1mWsxCXpK48PDrwlXCF2ioFhbSpmxcmWTvZbR0=",
                "jwt.access-token-expiration=900000"
})
class UserControllerWebMvcTest {

        @MockitoBean
        private SessionService sessionService;

        @MockitoBean
        private UserService userService;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtService jwtService;

        @Test
        void getUser_shouldReturnUnauthorizedWithoutToken()
                        throws Exception {

                mockMvc.perform(
                                get("/api/users/me")).andExpect(status().isUnauthorized());
        }

        @Test
        void getUser_shouldReturnUserId_whenTokenIsValid()
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
                                get("/api/users/me")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk());
        }

        @Test
        void adminEndpoint_shouldReturnForbidden_whenUserIsNotAdmin()
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
                                get("/api/users/admin-test")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminEndpoint_shouldReturnOk_whenUserIsAdmin()
                        throws Exception {

                // Arrange
                UUID userId = UUID.randomUUID();
                UUID sessionId = UUID.randomUUID();

                String token = jwtService.generateAccessToken(
                                userId,
                                "ADMIN",
                                sessionId);

                when(sessionService.isSessionValid(sessionId))
                                .thenReturn(true);

                // Act & Assert
                mockMvc.perform(
                                get("/api/users/admin-test")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk());
        }

        @Test
        void getUser_shouldReturnUnauthorized_whenSessionIsInvalid()
                        throws Exception {

                // Arrange
                UUID userId = UUID.randomUUID();
                UUID sessionId = UUID.randomUUID();

                String token = jwtService.generateAccessToken(
                                userId,
                                "USER",
                                sessionId);

                when(sessionService.isSessionValid(sessionId))
                                .thenReturn(false);

                // Act & Assert
                mockMvc.perform(
                                get("/api/users/me")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getUser_shouldReturnUnauthorized_afterSessionIsRevoked()
                        throws Exception {

                // Arrange
                UUID userId = UUID.randomUUID();
                UUID sessionId = UUID.randomUUID();

                String token = jwtService.generateAccessToken(
                                userId,
                                "USER",
                                sessionId);

                AtomicBoolean sessionValid = new AtomicBoolean(true);

                when(sessionService.isSessionValid(sessionId))
                                .thenAnswer(invocation -> sessionValid.get());

                // First request: session masih aktif
                mockMvc.perform(
                                get("/api/users/me")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isOk());

                // Simulasikan logout/revoke
                sessionValid.set(false);

                // Second request: JWT sama, tetapi session sudah revoked
                mockMvc.perform(
                                get("/api/users/me")
                                                .header(
                                                                "Authorization",
                                                                "Bearer " + token))
                                .andExpect(status().isUnauthorized());

                verify(sessionService, times(2))
                                .isSessionValid(sessionId);
        }

}