package com.ikhsan.securepaywallet.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.user.dto.req.RegisterRequestDto;
import com.ikhsan.securepaywallet.user.dto.res.UserResponseDto;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private UserService userService;

        @Test
        void getUser_shouldReturnUserResponse() {

                // Arrange
                UserEntity user = new UserEntity();

                user.setUsername("ikhsan");
                user.setEmail("ikhsan@example.com");
                user.setPhoneNumber("08123456789");

                // Act
                UserResponseDto result = userService.getUser(user);

                // Assert
                assertNotNull(result);

                assertEquals(
                                "ikhsan",
                                result.getUsername());

                assertEquals(
                                "ikhsan@example.com",
                                result.getEmail());

                assertEquals(
                                "08123456789",
                                result.getPhoneNumber());
        }

        @Test
        void register_shouldSaveUser_whenUsernameDoesNotExist() {

                // Arrange
                RegisterRequestDto request = new RegisterRequestDto();

                request.setUsername("ikhsan");
                request.setEmail("ikhsan@example.com");
                request.setPhoneNumber("08123456789");
                request.setPassword("password123");

                when(userRepository.existsByUsername("ikhsan"))
                                .thenReturn(false);

                // Act
                userService.register(request);

                // Assert
                ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

                verify(userRepository).save(captor.capture());

                UserEntity savedUser = captor.getValue();

                assertEquals(
                                "ikhsan",
                                savedUser.getUsername());

                assertEquals(
                                "ikhsan@example.com",
                                savedUser.getEmail());

                assertEquals(
                                "08123456789",
                                savedUser.getPhoneNumber());

                assertNotNull(savedUser.getPassword());

                assertTrue(
                                BCrypt.checkpw(
                                                "password123",
                                                savedUser.getPassword()));

                verify(userRepository)
                                .existsByUsername("ikhsan");
        }

        @Test
        void register_shouldThrowException_whenUsernameAlreadyExists() {

                // Arrange
                RegisterRequestDto request = new RegisterRequestDto();

                request.setUsername("ikhsan");
                request.setEmail("ikhsan@example.com");
                request.setPhoneNumber("08123456789");
                request.setPassword("password123");

                when(userRepository.existsByUsername("ikhsan"))
                                .thenReturn(true);

                // Act
                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> userService.register(request));

                // Assert
                assertEquals(
                                HttpStatus.BAD_REQUEST,
                                exception.getStatusCode());

                assertEquals(
                                "username already exist!",
                                exception.getReason());

                verify(userRepository)
                                .existsByUsername("ikhsan");

                verify(userRepository, never())
                                .save(any(UserEntity.class));
        }
}