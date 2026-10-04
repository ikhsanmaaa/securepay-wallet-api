package com.ikhsan.securepaywallet.wallet.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.ikhsan.securepaywallet.wallet.dto.res.WalletResponse;
import com.ikhsan.securepaywallet.wallet.service.WalletService;

@ActiveProfiles("test")
@WebMvcTest(controllers = WalletController.class)
@ContextConfiguration(classes = {
        WalletController.class,
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
class WalletControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void getWallet_shouldReturnUnauthorized_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/wallet/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWallet_shouldReturnWallet_whenTokenIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "USER", sessionId);
        when(sessionService.isSessionValid(sessionId)).thenReturn(true);

        WalletResponse walletResponse = WalletResponse.builder()
                .id(walletId)
                .userId(userId)
                .balance(BigDecimal.valueOf(100.00))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(walletService.getWalletByUserId(userId)).thenReturn(walletResponse);

        mockMvc.perform(get("/api/wallet/me")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(walletId.toString()))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.balance").value(100.00))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(walletService).getWalletByUserId(userId);
    }
}
