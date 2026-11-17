package com.ikhsan.securepaywallet.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "refresh token tidak boleh kosong")
    private String refreshToken;
}
