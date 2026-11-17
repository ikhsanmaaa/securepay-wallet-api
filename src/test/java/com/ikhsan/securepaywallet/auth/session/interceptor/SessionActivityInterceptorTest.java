package com.ikhsan.securepaywallet.auth.session.interceptor;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import com.ikhsan.securepaywallet.auth.session.annotation.SessionActivity;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class SessionActivityInterceptorTest {

    @Mock
    private SessionService sessionService;

    private SessionActivityInterceptor interceptor;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {

        mocks = MockitoAnnotations.openMocks(this);

        interceptor = new SessionActivityInterceptor(
                sessionService);

        SecurityContextHolder.clearContext();
    }

    @Test
    void afterCompletion_shouldUpdateActivity_whenEndpointHasAnnotationAndRequestIsSuccessful()
            throws Exception {

        // Arrange
        UUID sessionId = UUID.randomUUID();

        setAuthentication(sessionId);

        HandlerMethod handlerMethod = new HandlerMethod(
                new TestController(),
                TestController.class.getMethod("annotatedEndpoint"));

        HttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        interceptor.afterCompletion(
                request,
                response,
                handlerMethod,
                null);

        // Assert
        verify(sessionService)
                .updateActivity(sessionId);
    }

    @Test
    void afterCompletion_shouldNotUpdateActivity_whenEndpointDoesNotHaveAnnotation()
            throws Exception {

        // Arrange
        UUID sessionId = UUID.randomUUID();

        setAuthentication(sessionId);

        HandlerMethod handlerMethod = new HandlerMethod(
                new TestController(),
                TestController.class.getMethod("normalEndpoint"));

        HttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        interceptor.afterCompletion(
                request,
                response,
                handlerMethod,
                null);

        // Assert
        verifyNoInteractions(sessionService);
    }

    @Test
    void afterCompletion_shouldNotUpdateActivity_whenResponseIsClientError()
            throws Exception {

        // Arrange
        UUID sessionId = UUID.randomUUID();

        setAuthentication(sessionId);

        HandlerMethod handlerMethod = new HandlerMethod(
                new TestController(),
                TestController.class.getMethod("annotatedEndpoint"));

        HttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(400);

        // Act
        interceptor.afterCompletion(
                request,
                response,
                handlerMethod,
                null);

        // Assert
        verifyNoInteractions(sessionService);
    }

    @Test
    void afterCompletion_shouldNotUpdateActivity_whenResponseIsServerError()
            throws Exception {

        // Arrange
        UUID sessionId = UUID.randomUUID();

        setAuthentication(sessionId);

        HandlerMethod handlerMethod = new HandlerMethod(
                new TestController(),
                TestController.class.getMethod("annotatedEndpoint"));

        HttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(500);

        // Act
        interceptor.afterCompletion(
                request,
                response,
                handlerMethod,
                null);

        // Assert
        verifyNoInteractions(sessionService);
    }

    @Test
    void afterCompletion_shouldNotUpdateActivity_whenExceptionOccurs()
            throws Exception {

        // Arrange
        UUID sessionId = UUID.randomUUID();

        setAuthentication(sessionId);

        HandlerMethod handlerMethod = new HandlerMethod(
                new TestController(),
                TestController.class.getMethod("annotatedEndpoint"));

        HttpServletRequest request = new MockHttpServletRequest();

        HttpServletResponse response = new MockHttpServletResponse();

        Exception exception = new RuntimeException("Something went wrong");

        // Act
        interceptor.afterCompletion(
                request,
                response,
                handlerMethod,
                exception);

        // Assert
        verifyNoInteractions(sessionService);
    }

    @Test
    void afterCompletion_shouldNotUpdateActivity_whenAuthenticationIsMissing()
            throws Exception {

        // Arrange
        SecurityContextHolder.clearContext();

        HandlerMethod handlerMethod = new HandlerMethod(
                new TestController(),
                TestController.class.getMethod("annotatedEndpoint"));

        HttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        interceptor.afterCompletion(
                request,
                response,
                handlerMethod,
                null);

        // Assert
        verifyNoInteractions(sessionService);
    }

    @Test
    void afterCompletion_shouldNotUpdateActivity_whenAuthenticationDetailsIsNotSessionId()
            throws Exception {

        // Arrange
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                UUID.randomUUID(),
                null,
                null);

        authentication.setDetails("invalid-session-id");

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        HandlerMethod handlerMethod = new HandlerMethod(
                new TestController(),
                TestController.class.getMethod("annotatedEndpoint"));

        HttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setStatus(200);

        // Act
        interceptor.afterCompletion(
                request,
                response,
                handlerMethod,
                null);

        // Assert
        verifyNoInteractions(sessionService);
    }

    @AfterEach
    void tearDown() throws Exception {

        SecurityContextHolder.clearContext();

        mocks.close();
    }

    private void setAuthentication(UUID sessionId) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                UUID.randomUUID(),
                null,
                null);

        authentication.setDetails(sessionId);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    static class TestController {

        @SessionActivity
        public void annotatedEndpoint() {
        }

        public void normalEndpoint() {
        }
    }
}