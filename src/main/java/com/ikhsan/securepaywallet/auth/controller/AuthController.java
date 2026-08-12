package com.ikhsan.securepaywallet.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikhsan.securepaywallet.auth.dto.req.LoginRequest;
import com.ikhsan.securepaywallet.auth.dto.req.RegisterUserRequest;
import com.ikhsan.securepaywallet.auth.dto.res.TokenResponse;
import com.ikhsan.securepaywallet.auth.service.AuthService;
import com.ikhsan.securepaywallet.common.dto.WebResponse;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Auth", description = "User management APIs")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse userResponse = authService.register(request);
        return WebResponse.<UserResponse>builder().data(userResponse).build();
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {

        TokenResponse tokenResponse = authService.login(request);

        return WebResponse.<TokenResponse>builder().data(tokenResponse).build();
    }

    // @DeleteMapping(path = "/logout")
    // public WebResponse<String> logout(UserEntity user) {
    // authService.logout(user);
    // return WebResponse.<String>builder().data("ok").build();
    // }
}
