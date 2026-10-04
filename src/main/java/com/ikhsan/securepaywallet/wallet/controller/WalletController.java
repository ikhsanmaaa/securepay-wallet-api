package com.ikhsan.securepaywallet.wallet.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikhsan.securepaywallet.auth.session.annotation.SessionActivity;
import com.ikhsan.securepaywallet.common.dto.WebResponse;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletResponse;
import com.ikhsan.securepaywallet.wallet.service.WalletService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Wallet", description = "Wallet management APIs")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @SessionActivity
    @Operation(summary = "Get current user wallet", description = "Returns wallet information for authenticated user")
    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<WalletResponse> getWallet(Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();
        WalletResponse result = walletService.getWalletByUserId(userId);

        return WebResponse.<WalletResponse>builder().data(result).build();
    }
}
