package com.ikhsan.securepaywallet.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.common.service.ValidateService;
import com.ikhsan.securepaywallet.user.dto.req.RegisterRequestDto;
import com.ikhsan.securepaywallet.user.dto.res.UserResponseDto;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(ValidateService validateService, UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDto getUser(UserEntity user) {
        return UserResponseDto.builder().username(user.getUsername()).email(user.getEmail())
                .phoneNumber(user.getPhoneNumber()).build();
    }

    @Transactional
    public void register(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exist!");
        }

        var user = new UserEntity();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));

        userRepository.save(user);
    }
}
