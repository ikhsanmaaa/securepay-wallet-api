package com.ikhsan.securepaywallet.auth.security;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ikhsan.securepaywallet.auth.session.service.SessionService;

class SessionActivityFilterTest {

    private SessionService sessionService;

    private SessionActivityFilter filter;

    private FilterChain filterChain;

    @BeforeEach
    void setUp() {

        sessionService = mock(SessionService.class);

        filter = new SessionActivityFilter(
                sessionService);

        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldUpdateActivity_whenRequestIsSuccessful()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null);

        authentication.setDetails(sessionId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verify(sessionService)
                .updateActivity(sessionId);

        verify(filterChain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void doFilterInternal_shouldUpdateActivity_whenResponseIsRedirect()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null);

        authentication.setDetails(sessionId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(302);

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verify(sessionService)
                .updateActivity(sessionId);
    }

    @Test
    void doFilterInternal_shouldNotUpdateActivity_whenResponseIsClientError()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null);

        authentication.setDetails(sessionId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(400);

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verify(sessionService, never())
                .updateActivity(any());
    }

    @Test
    void doFilterInternal_shouldNotUpdateActivity_whenResponseIsServerError()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null);

        authentication.setDetails(sessionId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(500);

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verify(sessionService, never())
                .updateActivity(any());
    }

    @Test
    void doFilterInternal_shouldNotUpdateActivity_whenAuthenticationDoesNotExist()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verifyNoInteractions(sessionService);

        verify(filterChain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void doFilterInternal_shouldNotUpdateActivity_whenAuthenticationDetailsIsNotSessionId()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null);

        authentication.setDetails("not-a-session-id");

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verify(sessionService, never())
                .updateActivity(any());
    }

    @Test
    void doFilterInternal_shouldUpdateActivityAfterFilterChain()
            throws ServletException, IOException {

        // Arrange
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null);

        authentication.setDetails(sessionId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        filter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        var inOrder = inOrder(
                filterChain,
                sessionService);

        inOrder.verify(filterChain)
                .doFilter(
                        request,
                        response);

        inOrder.verify(sessionService)
                .updateActivity(sessionId);
    }
}