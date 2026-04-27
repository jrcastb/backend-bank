package com.devsu.backendbank.domain.model;

import com.devsu.backendbank.domain.enums.TransactionTypeDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDomain(
        Long id,
        Long accountId,
        LocalDateTime fecha,
        TransactionTypeDomain tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo,
        LocalDateTime createdAt,
        String numeroCuenta
) {
}

