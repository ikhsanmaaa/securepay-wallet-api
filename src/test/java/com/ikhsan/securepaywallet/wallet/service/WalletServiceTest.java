package com.ikhsan.securepaywallet.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ikhsan.securepaywallet.enumerate.WalletStatus;
import com.ikhsan.securepaywallet.user.entity.UserEntity;
import com.ikhsan.securepaywallet.wallet.dto.res.WalletResponse;
import com.ikhsan.securepaywallet.wallet.entity.WalletEntity;
import com.ikhsan.securepaywallet.wallet.repo.WalletRepository;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void createWallet_shouldCreateAndReturnNewWallet_whenNotExists() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(WalletEntity.class))).thenAnswer(invocation -> {
            WalletEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        WalletEntity result = walletService.createWallet(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(WalletStatus.ACTIVE, result.getStatus());
        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    void createWallet_shouldReturnExistingWallet_whenAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);

        WalletEntity existing = new WalletEntity();
        existing.setId(UUID.randomUUID());
        existing.setUser(user);
        existing.setBalance(BigDecimal.valueOf(50));
        existing.setStatus(WalletStatus.ACTIVE);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        WalletEntity result = walletService.createWallet(user);

        assertEquals(existing, result);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void getWalletByUserId_shouldReturnWalletResponse_whenFound() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        WalletEntity wallet = new WalletEntity();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(250.50));
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setCreatedAt(Instant.now());
        wallet.setUpdatedAt(Instant.now());

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getWalletByUserId(userId);

        assertNotNull(response);
        assertEquals(walletId, response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(BigDecimal.valueOf(250.50), response.getBalance());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void getWalletByUserId_shouldThrowNotFound_whenMissing() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> walletService.getWalletByUserId(userId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
