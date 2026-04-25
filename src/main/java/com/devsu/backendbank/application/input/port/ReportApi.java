package com.devsu.backendbank.application.input.port;

import com.devsu.backendbank.domain.model.ReportResultDomain;

import java.time.LocalDate;

public interface ReportApi {

    ReportResultDomain generateReport(Long clientId, LocalDate fechaDesde, LocalDate fechaHasta, String formato);
}
