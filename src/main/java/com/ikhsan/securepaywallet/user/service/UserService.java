package com.ikhsan.securepaywallet.user.service;

import org.springframework.stereotype.Service;

import com.ikhsan.securepaywallet.common.service.ValidateService;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.repo.UserRepository;

@Service
public class UserService implements IUser {

    private final UserRepository userRepository;

    public UserService(ValidateService validateService, UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUser(UserEntity user) {
        return UserResponse.builder().username(user.getUsername()).email(user.getEmail())
                .phoneNumber(user.getPhoneNumber()).build();
    }
}
