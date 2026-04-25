package com.devsu.backendbank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReportItemDomain(
        LocalDateTime fecha,
        String cliente,
        String numeroCuenta,
        String tipo,
        BigDecimal saldoInicial,
        Boolean estado,
        BigDecimal movimiento,
        BigDecimal saldoDisponible
) {
}

