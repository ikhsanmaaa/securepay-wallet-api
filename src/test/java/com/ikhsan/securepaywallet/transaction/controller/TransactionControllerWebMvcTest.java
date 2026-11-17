package com.ikhsan.securepaywallet.transaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ikhsan.securepaywallet.auth.security.JwtAuthenticationFilter;
import com.ikhsan.securepaywallet.auth.security.JwtService;
import com.ikhsan.securepaywallet.auth.session.interceptor.SessionActivityInterceptor;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.common.config.SecurityConfig;
import com.ikhsan.securepaywallet.common.config.WebMvcConfig;
import com.ikhsan.securepaywallet.transaction.dto.res.TransactionResponse;
import com.ikhsan.securepaywallet.transaction.service.TransactionService;

@ActiveProfiles("test")
@WebMvcTest(controllers = TransactionController.class)
@ContextConfiguration(classes = {
        TransactionController.class,
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
class TransactionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void topUp_shouldReturnUnauthorized_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/transactions/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100000.00}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void topUp_shouldReturnCreated_whenRequestValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);
        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        TransactionResponse response = TransactionResponse.builder()
                .id(transactionId)
                .referenceNumber("TOPUP-" + transactionId)
                .type("TOPUP")
                .status("SUCCESS")
                .amount(new BigDecimal("100000.00"))
                .fee(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        when(transactionService.topUp(eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions/topup")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100000.00,\"description\":\"top up\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.data.type").value("TOPUP"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.amount").value(100000.00));

        verify(transactionService).topUp(eq(userId), any());
    }

    @Test
    void topUp_shouldReturnBadRequest_whenAmountMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);
        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        mockMvc.perform(post("/api/transactions/topup")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void topUp_shouldReturnBadRequest_whenAmountHasTooManyDecimals() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);
        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        mockMvc.perform(post("/api/transactions/topup")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.001}"))
                .andExpect(status().isBadRequest());
    }
}
