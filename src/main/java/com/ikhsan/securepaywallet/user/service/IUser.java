package com.ikhsan.securepaywallet.user.service;

import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;

public interface IUser {

    UserResponse getUser(UserEntity user);

}
