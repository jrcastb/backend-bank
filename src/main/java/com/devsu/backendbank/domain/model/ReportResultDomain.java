package com.devsu.backendbank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportResultDomain(
        Long clientId,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        List<ReportItemDomain> items
) {
}

