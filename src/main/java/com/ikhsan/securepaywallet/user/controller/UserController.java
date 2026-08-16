package com.ikhsan.securepaywallet.user.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikhsan.securepaywallet.common.dto.WebResponse;
import com.ikhsan.securepaywallet.user.dto.res.UserResponse;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users", description = "User management APIs")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current user", description = "Returns user information")
    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<UserResponse> getCurrentUser(Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        UserResponse userResponse = userService.getUserById(userId);
        return WebResponse.<UserResponse>builder().data(userResponse).build();
    }

    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public WebResponse<String> adminTest() {

        return WebResponse.<String>builder()
                .data("admin access")
                .build();
    }
}
