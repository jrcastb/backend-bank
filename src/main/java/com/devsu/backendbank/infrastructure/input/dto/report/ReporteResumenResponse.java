package com.devsu.backendbank.infrastructure.input.dto.report;

import java.math.BigDecimal;

public record ReporteResumenResponse(
        BigDecimal totalDebitos,
        BigDecimal totalCreditos
) {
}

