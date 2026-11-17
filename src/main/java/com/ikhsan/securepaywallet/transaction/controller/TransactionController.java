package com.ikhsan.securepaywallet.transaction.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ikhsan.securepaywallet.auth.session.annotation.SessionActivity;
import com.ikhsan.securepaywallet.common.dto.WebResponse;
import com.ikhsan.securepaywallet.transaction.dto.req.TopupRequest;
import com.ikhsan.securepaywallet.transaction.dto.res.TransactionResponse;
import com.ikhsan.securepaywallet.transaction.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Transaction", description = "Money movement APIs")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @SessionActivity
    @Operation(summary = "Top up wallet", description = "Adds balance to the authenticated user wallet (simulated)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/topup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TransactionResponse> topUp(Authentication authentication,
                                                  @Valid @RequestBody TopupRequest request) {

        UUID userId = (UUID) authentication.getPrincipal();
        TransactionResponse result = transactionService.topUp(userId, request);

        return WebResponse.<TransactionResponse>builder().data(result).build();
    }
}
