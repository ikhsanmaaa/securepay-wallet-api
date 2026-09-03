package com.ikhsan.securepaywallet.auth.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    private String refreshToken;
}
