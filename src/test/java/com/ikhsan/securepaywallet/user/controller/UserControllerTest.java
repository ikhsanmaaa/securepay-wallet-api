package com.ikhsan.securepaywallet.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ikhsan.securepaywallet.common.dto.WebResponse;
import com.ikhsan.securepaywallet.user.dto.req.RegisterRequestDto;
import com.ikhsan.securepaywallet.user.dto.res.UserResponseDto;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserEntity user;
    private RegisterRequestDto registerRequest;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        registerRequest = new RegisterRequestDto();
    }

    @Test
    void getUser_shouldReturnUserResponse() {
        // Arrange
        UserResponseDto userResponse = mock(UserResponseDto.class);

        when(userService.getUser(user))
                .thenReturn(userResponse);

        // Act
        WebResponse<UserResponseDto> response = userController.getUser(user);

        // Assert
        assertNotNull(response);
        assertEquals(userResponse, response.getData());

        verify(userService).getUser(user);
    }

    @Test
    void register_shouldCallUserServiceAndReturnOk() {
        // Arrange
        // userService.register() adalah void,
        // jadi tidak perlu when()

        // Act
        WebResponse<String> response = userController.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("ok", response.getData());

        verify(userService).register(registerRequest);
    }
}