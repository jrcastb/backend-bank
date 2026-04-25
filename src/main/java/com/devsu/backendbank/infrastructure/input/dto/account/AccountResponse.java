package com.devsu.backendbank.infrastructure.input.dto.account;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long clienteId,
        String numeroCuenta,
        AccountTypeDto tipoCuenta,
        BigDecimal saldoInicial,
        Boolean estado
) {
}

