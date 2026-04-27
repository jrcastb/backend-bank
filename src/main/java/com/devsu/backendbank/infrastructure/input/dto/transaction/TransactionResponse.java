package com.devsu.backendbank.infrastructure.input.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String numeroCuenta,
        LocalDateTime fecha,
        TransactionTypeDto tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo
) {
}

