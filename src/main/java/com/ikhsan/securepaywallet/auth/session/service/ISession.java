package com.ikhsan.securepaywallet.auth.session.service;

import java.util.UUID;

import com.ikhsan.securepaywallet.auth.session.entity.SessionEntity;
import com.ikhsan.securepaywallet.user.entity.UserEntity;

public interface ISession {

    public SessionEntity createSession(UserEntity user);

    public boolean isSessionValid(UUID sessionId);

    public void revokeSession(UUID sessionId);

    public void updateActivity(UUID sessionId);

}
