package com.ikhsan.securepaywallet.user.service;

import java.util.UUID;

import com.ikhsan.securepaywallet.user.dto.res.UserResponse;

public interface IUser {

    UserResponse getUserById(UUID userID);

}
