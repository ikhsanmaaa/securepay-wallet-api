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
import com.ikhsan.securepaywallet.user.dto.req.EditRequestDto;
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
    void getCurrentUser_shouldReturnCurrentUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .username("ikhsan")
                .email("ikhsan@mail.com")
                .role("USER")
                .build();

        when(userService.getUserById(userId)).thenReturn(userResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null);

        // Act
        WebResponse<UserResponse> response = userController.getCurrentUser(authentication);

        // Assert
        assertNotNull(response);
        assertEquals(userResponse, response.getData());

        verify(userService).getUserById(userId);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        EditRequestDto request = new EditRequestDto(
                "newuser", "New Name", "new@mail.com", "08222222222");

        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .username("newuser")
                .email("new@mail.com")
                .phoneNumber("08222222222")
                .role("USER")
                .build();

        when(userService.updateUser(userId, request)).thenReturn(userResponse);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null);

        // Act
        WebResponse<UserResponse> response = userController.updateUser(authentication, request);

        // Assert
        assertNotNull(response);
        assertEquals(userResponse, response.getData());
        assertEquals("newuser", response.getData().getUsername());
        assertEquals("new@mail.com", response.getData().getEmail());
        assertEquals("08222222222", response.getData().getPhoneNumber());

        verify(userService).updateUser(userId, request);
    }
}
