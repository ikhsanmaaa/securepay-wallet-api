package com.ikhsan.securepaywallet.auth.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ikhsan.securepaywallet.auth.session.service.SessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SessionActivityFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    public SessionActivityFilter(
            SessionService sessionService) {

        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        filterChain.doFilter(
                request,
                response);

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            return;
        }

        Object details = authentication.getDetails();

        if (!(details instanceof UUID sessionId)) {
            return;
        }

        if (response.getStatus() >= 200 &&
                response.getStatus() < 400) {

            sessionService.updateActivity(sessionId);
        }
    }
}
