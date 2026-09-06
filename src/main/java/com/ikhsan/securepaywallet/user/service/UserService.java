package com.ikhsan.securepaywallet.user.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.common.service.ValidateService;
import com.ikhsan.securepaywallet.user.dto.req.EditRequestDto;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService implements IUser {

    private final UserRepository userRepository;

    public UserService(ValidateService validateService, UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(UUID userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .build();

    }

    @Transactional
    public UserResponse updateUser(UUID userId, EditRequestDto request) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists!");
        }

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists!");
        }

        if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "phone number already exists!");
        }

        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (Objects.nonNull(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .build();
    }
}
