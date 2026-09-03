package com.ikhsan.securepaywallet.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.auth.dto.req.RefreshTokenRequest;
import com.ikhsan.securepaywallet.auth.dto.res.TokenResponse;
import com.ikhsan.securepaywallet.auth.security.JwtAuthenticationFilter;
import com.ikhsan.securepaywallet.auth.security.SessionActivityFilter;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.auth.service.AuthService;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = AuthController.class)
class AuthControllerWebMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockitoBean
        private SessionActivityFilter sessionActivityFilter;

        @MockitoBean
        private SessionService sessionService;

        @Test
        void refresh_shouldReturnNewAccessToken_whenRefreshTokenIsValid()
                        throws Exception {

                // Arrange
                TokenResponse response = TokenResponse.builder()
                                .accessToken("new-access-token")
                                .build();

                when(authService.refresh(any(RefreshTokenRequest.class)))
                                .thenReturn(response);

                // Act & Assert
                mockMvc.perform(
                                post("/api/auth/refresh")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "refreshToken": "valid-refresh-token"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.data.accessToken")
                                                                .value("new-access-token"));

                verify(authService)
                                .refresh(any(RefreshTokenRequest.class));
        }

        @Test
        void refresh_shouldReturnUnauthorized_whenRefreshTokenIsInvalid()
                        throws Exception {

                // Arrange
                when(authService.refresh(any(RefreshTokenRequest.class)))
                                .thenThrow(
                                                new ResponseStatusException(
                                                                HttpStatus.UNAUTHORIZED,
                                                                "invalid refresh token"));

                // Act & Assert
                mockMvc.perform(
                                post("/api/auth/refresh")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "refreshToken": "invalid-refresh-token"
                                                                }
                                                                """))
                                .andExpect(status().isUnauthorized());

                verify(authService)
                                .refresh(any(RefreshTokenRequest.class));
        }
}
