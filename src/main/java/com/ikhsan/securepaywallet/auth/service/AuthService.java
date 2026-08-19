package com.ikhsan.securepaywallet.auth.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.auth.dto.req.ChangePasswordRequest;
import com.ikhsan.securepaywallet.auth.dto.req.LoginRequest;
import com.ikhsan.securepaywallet.auth.dto.req.RegisterUserRequest;
import com.ikhsan.securepaywallet.auth.dto.res.TokenResponse;
import com.ikhsan.securepaywallet.auth.security.JwtService;
import com.ikhsan.securepaywallet.auth.session.entity.SessionEntity;
import com.ikhsan.securepaywallet.auth.session.service.SessionService;
import com.ikhsan.securepaywallet.enumerate.Role;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService implements IAuth {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "email already exists");
        }

        var user = new UserEntity();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return UserResponse.builder().username(user.getUsername()).email(user.getEmail())
                .phoneNumber(user.getPhoneNumber()).build();
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {

        UserEntity user = userRepository.findFirstByUsername(request.getUsername()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username or password is invalid"));

        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!isPasswordMatch) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username or password is invalid");
        }

        SessionEntity session = sessionService.createSession(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name(), session.getId());

        return TokenResponse.builder().token(accessToken).build();

    }

    @Transactional
    public void logout(UUID sessionId) {
        sessionService.revokeSession(sessionId);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = userRepository.findFirstByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not Found!"));
        boolean isPasswordMatch = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (!isPasswordMatch) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "password is invalid!");
        }

        if (request.getNewPassword() == request.getNewConfirmPassword()) {
            String hashNewPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(hashNewPassword);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "new password didnt match");
        }

    }
}
