package com.devsu.backendbank.infrastructure.input.dto.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionCreateRequest(
        @NotNull Long cuentaId,
        @NotNull TransactionTypeDto tipoMovimiento,
        @NotNull @Positive BigDecimal valor
) {
}

