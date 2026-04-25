package com.devsu.backendbank.infrastructure.input.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountCreateRequest(
        @NotNull Long clienteId,
        @NotBlank @Size(max = 30) String numeroCuenta,
        @NotNull AccountTypeDto tipoCuenta,
        @NotNull @PositiveOrZero BigDecimal saldoInicial,
        Boolean estado
) {
}

