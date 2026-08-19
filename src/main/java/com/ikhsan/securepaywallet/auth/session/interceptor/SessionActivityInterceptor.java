package com.ikhsan.securepaywallet.auth.session.interceptor;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ikhsan.securepaywallet.auth.session.annotation.SessionActivity;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class SessionActivityInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    public SessionActivityInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }

        if (!handlerMethod.hasMethodAnnotation(SessionActivity.class)) {
            return;
        }

        if (exception != null) {
            return;
        }

        if (response.getStatus() >= 400) {
            return;
        }

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

        sessionService.updateActivity(sessionId);
    }
}