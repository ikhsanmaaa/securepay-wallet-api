package com.ikhsan.securepaywallet.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.enumerate.Role;
import com.ikhsan.securepaywallet.user.dto.req.EditRequestDto;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ── getUserById ──────────────────────────────────────────────────────────

    @Test
    void getUserById_shouldReturnUserResponse_whenUserExists() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("ikhsan");
        user.setEmail("ikhsan@mail.com");
        user.setPhoneNumber("08123456789");
        user.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("ikhsan", response.getUsername());
        assertEquals("ikhsan@mail.com", response.getEmail());
        assertEquals("08123456789", response.getPhoneNumber());
        assertEquals("USER", response.getRole());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_shouldThrowUsernameNotFoundException_whenUserDoesNotExist() {

        // Arrange
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
    }

    // ── updateUser ───────────────────────────────────────────────────────────

    @Test
    void updateUser_shouldSaveAndReturnUpdatedUser_whenRequestIsValid() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("olduser");
        user.setEmail("old@mail.com");
        user.setPhoneNumber("08111111111");
        user.setRole(Role.USER);

        EditRequestDto request = new EditRequestDto(
                "newuser", "New Name", "new@mail.com", "08222222222");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("newuser", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("new@mail.com", userId)).thenReturn(false);
        when(userRepository.existsByPhoneNumberAndIdNot("08222222222", userId)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        // Act
        UserResponse response = userService.updateUser(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("newuser", response.getUsername());
        assertEquals("new@mail.com", response.getEmail());
        assertEquals("08222222222", response.getPhoneNumber());

        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowNotFound_whenUserDoesNotExist() {

        // Arrange
        UUID userId = UUID.randomUUID();

        EditRequestDto request = new EditRequestDto(
                "username", "Full Name", "email@mail.com", "08111111111");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(userRepository, never()).save(null);
    }

    @Test
    void updateUser_shouldThrowConflict_whenUsernameAlreadyTakenByAnotherUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setRole(Role.USER);

        EditRequestDto request = new EditRequestDto(
                "takenuser", "Full Name", "email@mail.com", "08111111111");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("takenuser", userId)).thenReturn(true);

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUser_shouldThrowConflict_whenEmailAlreadyTakenByAnotherUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setRole(Role.USER);

        EditRequestDto request = new EditRequestDto(
                "username", "Full Name", "taken@mail.com", "08111111111");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("username", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("taken@mail.com", userId)).thenReturn(true);

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUser_shouldThrowConflict_whenPhoneNumberAlreadyTakenByAnotherUser() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setRole(Role.USER);

        EditRequestDto request = new EditRequestDto(
                "username", "Full Name", "email@mail.com", "08999999999");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("username", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("email@mail.com", userId)).thenReturn(false);
        when(userRepository.existsByPhoneNumberAndIdNot("08999999999", userId)).thenReturn(true);

        // Act & Assert
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateUser(userId, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUser_shouldSave_whenPhoneNumberIsNull() {

        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("username");
        user.setEmail("email@mail.com");
        user.setRole(Role.USER);

        // phoneNumber null — optional field, boleh kosong
        EditRequestDto request = new EditRequestDto("username", "Full Name", "email@mail.com", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("username", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("email@mail.com", userId)).thenReturn(false);
        when(userRepository.existsByPhoneNumberAndIdNot(null, userId)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        // Act
        UserResponse response = userService.updateUser(userId, request);

        // Assert
        assertNotNull(response);
        verify(userRepository).save(user);
    }
}
