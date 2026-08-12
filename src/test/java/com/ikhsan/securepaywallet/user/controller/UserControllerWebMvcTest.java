package com.ikhsan.securepaywallet.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                "hwDe+1mWsxCXpK48PDrwlXCF2ioFhbSpmxcmWTvZbR0=",
                "jwt.access-token-expiration=900000"
})
class UserControllerWebMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtService jwtService;

        @MockitoBean
        private UserService userService;

        @Test
        void getUser_shouldReturnUnauthorizedWithoutToken()
                        throws Exception {

                mockMvc.perform(
                                get("/api/users/me"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getUser_shouldReturnUserId_whenTokenIsValid()
                        throws Exception {

                UUID userId = UUID.randomUUID();

                String token = jwtService.generateAccessToken(
                                userId,
                                "USER");

                mockMvc.perform(
                                get("/api/users/me")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk());
        }
}