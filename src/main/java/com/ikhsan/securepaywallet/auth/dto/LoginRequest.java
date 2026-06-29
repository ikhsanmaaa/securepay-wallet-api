package com.ikhsan.securepaywallet.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoginRequest {

    @NotBlank(message = "username tidak boleh kosong")
    private String username;

    @NotBlank(message = "password tidak boleh kosong")
    private String password;
}
