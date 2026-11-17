package com.ikhsan.securepaywallet.transaction.dto.req;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TopupRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    private String description;
}
