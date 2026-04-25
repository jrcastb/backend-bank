package com.devsu.backendbank.infrastructure.input.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteItemResponse(
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

