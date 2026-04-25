package com.devsu.backendbank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDomain(
        Long id,
        Long clientId,
        String numeroCuenta,
        AccountTypeDomain tipoCuenta,
        BigDecimal saldoInicial,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

