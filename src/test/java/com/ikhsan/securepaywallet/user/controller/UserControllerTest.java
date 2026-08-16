package com.ikhsan.securepaywallet.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.ikhsan.securepaywallet.common.dto.WebResponse;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.service.UserService;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {

        userService = mock(UserService.class);

        userController = new UserController(userService);
    }

    @Test
    void getUser_shouldReturnCurrentUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .build();

        when(userService.getUserById(userId))
                .thenReturn(userResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null);

        // Act
        WebResponse<UserResponse> response = userController.getCurrentUser(
                authentication);

        // Assert
        assertNotNull(response);
        assertEquals(
                userResponse,
                response.getData());

        verify(userService)
                .getUserById(userId);
    }
}