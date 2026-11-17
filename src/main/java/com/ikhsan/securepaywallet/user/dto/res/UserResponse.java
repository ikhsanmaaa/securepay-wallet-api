package com.ikhsan.securepaywallet.user.dto.res;

import java.util.UUID;

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
public class UserResponse {

    private UUID id;

    private String username;

    private String email;

    private String phoneNumber;

    private String role;
}
