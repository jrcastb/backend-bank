package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ReportQueryPort {
    Page<TransactionReportProjection> findReportByClientAndDateRange(Long clientId,
                                                                     LocalDateTime fechaDesde,
                                                                     LocalDateTime fechaHasta,
                                                                     Pageable pageable);
}

