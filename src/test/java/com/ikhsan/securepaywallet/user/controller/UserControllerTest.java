package com.ikhsan.securepaywallet.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.ikhsan.securepaywallet.common.dto.WebResponse;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(null);
    }

    @Test
    void getUserDummy_shouldReturnAuthenticatedUserName() {

        // Arrange
        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("123");

        // Act
        WebResponse<String> response = userController.getUserDummy(authentication);

        // Assert
        assertNotNull(response);
        assertEquals("123", response.getData());

        verify(authentication).getName();
    }
}