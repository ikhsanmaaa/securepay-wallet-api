package com.ikhsan.securepaywallet.auth.service;

import com.ikhsan.securepaywallet.auth.dto.req.LoginRequest;
import com.ikhsan.securepaywallet.auth.dto.req.RegisterUserRequest;
import com.ikhsan.securepaywallet.auth.dto.res.TokenResponse;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;

public interface IAuth {

    UserResponse register(RegisterUserRequest registerUserRequest);

    TokenResponse login(LoginRequest loginRequestDto);

    // void logout(UserEntity user);
}
