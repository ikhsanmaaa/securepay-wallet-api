package com.ikhsan.securepaywallet.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.auth.security.JwtAuthenticationFilter;
import com.ikhsan.securepaywallet.auth.security.JwtService;
import com.ikhsan.securepaywallet.auth.session.interceptor.SessionActivityInterceptor;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.common.config.SecurityConfig;
import com.ikhsan.securepaywallet.common.config.WebMvcConfig;
import com.ikhsan.securepaywallet.user.dto.req.EditRequestDto;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.service.UserService;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = {
        UserController.class,
        SecurityConfig.class,
        WebMvcConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        SessionActivityInterceptor.class
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

    // ── GET /api/users/me ────────────────────────────────────────────────────

    @Test
    void getUser_shouldReturnUnauthorized_whenNoToken() throws Exception {

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUser_shouldReturnOk_whenTokenIsValid() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_shouldReturnUnauthorized_whenSessionIsInvalid() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUser_shouldReturnUnauthorized_afterSessionIsRevoked() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        AtomicBoolean sessionValid = new AtomicBoolean(true);
        when(sessionService.isSessionValid(sessionId))
                .thenAnswer(invocation -> sessionValid.get());

        // First request: session aktif
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Revoke session
        sessionValid.set(false);

        // Second request: token sama, session sudah revoked
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(sessionService, times(2)).isSessionValid(sessionId);
    }

    @Test
    void getUser_shouldUpdateSessionActivity_whenRequestIsSuccessful() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Assert
        verify(sessionService).updateActivity(sessionId);
    }

    // ── GET /api/users/admin-test ────────────────────────────────────────────

    @Test
    void adminEndpoint_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/admin-test")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldReturnOk_whenUserIsAdmin() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "ADMIN", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/admin-test")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_shouldNotUpdateSessionActivity_whenNotAnnotated() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "ADMIN", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act
        mockMvc.perform(get("/api/users/admin-test")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Assert: @SessionActivity tidak ada di endpoint ini
        verify(sessionService, never()).updateActivity(sessionId);
    }

    // ── PUT /api/users/me ────────────────────────────────────────────────────

    @Test
    void updateUser_shouldReturnUnauthorized_whenNoToken() throws Exception {

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "newuser",
                            "name": "New Name",
                            "email": "new@mail.com",
                            "phoneNumber": "08222222222"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser_shouldReturnOk_whenRequestIsValid() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .username("newuser")
                .email("new@mail.com")
                .phoneNumber("08222222222")
                .role("USER")
                .build();

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);
        when(userService.updateUser(eq(userId), any(EditRequestDto.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "newuser",
                            "name": "New Name",
                            "email": "new@mail.com",
                            "phoneNumber": "08222222222"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("new@mail.com"))
                .andExpect(jsonPath("$.data.phoneNumber").value("08222222222"));

        verify(userService).updateUser(eq(userId), any(EditRequestDto.class));
    }

    @Test
    void updateUser_shouldReturnBadRequest_whenUsernameIsTooShort() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act & Assert — username "ab" kurang dari 5 karakter
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "ab",
                            "name": "New Name",
                            "email": "new@mail.com"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void updateUser_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "newuser",
                            "name": "New Name",
                            "email": "not-a-valid-email"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void updateUser_shouldReturnBadRequest_whenRequiredFieldsAreMissing() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        // Act & Assert — username dan email wajib (@NotBlank)
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void updateUser_shouldReturnConflict_whenUsernameAlreadyExists() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);
        when(userService.updateUser(eq(userId), any(EditRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "username already exists!"));

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "takenuser",
                            "name": "New Name",
                            "email": "new@mail.com"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_shouldUpdateSessionActivity_whenRequestIsSuccessful() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);

        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .username("newuser")
                .email("new@mail.com")
                .role("USER")
                .build();

        when(sessionService.isSessionValid(sessionId)).thenReturn(true);
        when(userService.updateUser(eq(userId), any(EditRequestDto.class))).thenReturn(userResponse);

        // Act
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "newuser",
                            "name": "New Name",
                            "email": "new@mail.com"
                        }
                        """))
                .andExpect(status().isOk());

        // Assert: @SessionActivity harus trigger updateActivity
        verify(sessionService).updateActivity(sessionId);
    }
}
