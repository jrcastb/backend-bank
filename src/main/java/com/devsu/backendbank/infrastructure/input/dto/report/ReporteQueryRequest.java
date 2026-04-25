package com.devsu.backendbank.infrastructure.input.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReporteQueryRequest(
        @NotNull Long clienteId,
        @NotNull LocalDate fechaDesde,
        @NotNull LocalDate fechaHasta,
        FormatoReporteDto formato
) {
}

