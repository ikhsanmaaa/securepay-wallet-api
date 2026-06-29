package com.ikhsan.securepaywallet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "password tidak boleh kosong")
    private String username;

    @NotBlank(message = "password tidak boleh kosong")
    private String name;

    @Email(message = "email harus terdaftar")
    private String email;

    @NotBlank(message = "password tidak boleh kosong")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$", message = "Password minimal 8 karakter, mengandung 1 huruf kapital dan 1 angka")
    private String password;
}
