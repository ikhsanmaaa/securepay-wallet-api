package com.ikhsan.securepaywallet.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        filter = new JwtAuthenticationFilter(jwtService);
        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "USER";
        String token = "valid-token";

        when(jwtService.isValid(token))
                .thenReturn(true);

        when(jwtService.extractSubject(token))
                .thenReturn(userId.toString());

        when(jwtService.extractRole(token))
                .thenReturn(role);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                userId,
                authentication.getPrincipal());

        assertEquals(
                "ROLE_USER",
                authentication
                        .getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority());

        verify(filterChain).doFilter(
                request,
                response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationHeaderMissing()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(
                request,
                response,
                filterChain);

        // Assert
        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        verify(filterChain).doFilter(
                request,
                response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationIsNotBearer()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic abc123");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(
                request,
                response,
                filterChain);

        // Assert
        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        verify(filterChain).doFilter(
                request,
                response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenTokenIsInvalid()
            throws ServletException, IOException {

        // Arrange
        String token = "invalid-token";

        when(jwtService.isValid(token))
                .thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        verify(jwtService).isValid(token);

        verify(filterChain).doFilter(
                request,
                response);

        verify(jwtService, never())
                .extractSubject(anyString());

        verify(jwtService, never())
                .extractRole(anyString());
    }

    @Test
    void doFilterInternal_shouldSetAdminAuthority_whenRoleIsAdmin()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();
        String token = "valid-admin-token";

        when(jwtService.isValid(token))
                .thenReturn(true);

        when(jwtService.extractSubject(token))
                .thenReturn(userId.toString());

        when(jwtService.extractRole(token))
                .thenReturn("ADMIN");

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                userId,
                authentication.getPrincipal());

        assertEquals(
                "ROLE_ADMIN",
                authentication
                        .getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority());

        verify(filterChain).doFilter(
                request,
                response);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

}