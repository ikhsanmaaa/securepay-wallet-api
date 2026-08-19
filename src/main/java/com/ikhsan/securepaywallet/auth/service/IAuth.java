package com.ikhsan.securepaywallet.auth.service;

import java.util.UUID;

import com.ikhsan.securepaywallet.auth.dto.req.ChangePasswordRequest;
import com.ikhsan.securepaywallet.auth.dto.req.LoginRequest;
import com.ikhsan.securepaywallet.auth.dto.req.RegisterUserRequest;
import com.ikhsan.securepaywallet.auth.dto.res.TokenResponse;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;

public interface IAuth {

    UserResponse register(RegisterUserRequest registerUserRequest);

    TokenResponse login(LoginRequest loginRequestDto);

    void logout(UUID sessionId);

    void changePassword(String username, ChangePasswordRequest request);
}
