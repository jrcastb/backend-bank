package com.devsu.backendbank.infrastructure.input.dto.account;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountPatchRequest(
        @Size(max = 30) String numeroCuenta,
        AccountTypeDto tipoCuenta,
        @PositiveOrZero BigDecimal saldoInicial,
        Boolean estado
) {
}

