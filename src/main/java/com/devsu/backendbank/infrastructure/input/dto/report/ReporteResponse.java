 package com.devsu.backendbank.infrastructure.input.dto.report;

import java.time.LocalDate;
import java.util.List;

public record ReporteResponse(
        Long clienteId,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        ReporteResumenResponse resumen,
        List<ReporteItemResponse> items,
        String pdfBase64
) {
}

