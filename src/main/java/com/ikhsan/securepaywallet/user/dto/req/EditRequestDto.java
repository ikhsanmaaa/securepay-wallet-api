package com.ikhsan.securepaywallet.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EditRequestDto {

    @NotBlank
    @Size(min = 5, message = "minimum of five chars")
    private String username;

    @NotBlank
    @Size(min = 5, message = "minimum of five chars")
    private String name;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;
}
