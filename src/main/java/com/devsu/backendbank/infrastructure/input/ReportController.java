package com.devsu.backendbank.infrastructure.input;

import com.devsu.backendbank.application.input.port.ReportApi;
import com.devsu.backendbank.infrastructure.input.dto.report.ReporteResponse;
import com.devsu.backendbank.infrastructure.input.mapper.ReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
@Tag(name = "Reportes")
public class ReportController {

    private final ReportApi reportApi;
    private final ReportMapper reportMapper;

    public ReportController(ReportApi reportApi,
                            ReportMapper reportMapper) {
        this.reportApi = reportApi;
        this.reportMapper = reportMapper;
    }

    @GetMapping
    @Operation(summary = "Generar reporte por cliente y rango de fechas")
    public ReporteResponse generate(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "json") String formato
    ) {
        return reportMapper.toResponse(reportApi.generateReport(clienteId, fechaDesde, fechaHasta, formato));
    }
}
