package com.devsu.backendbank.infrastructure.input;

import com.devsu.backendbank.application.input.port.ReportApi;
import com.devsu.backendbank.infrastructure.exception.TechnicalException;
import com.devsu.backendbank.infrastructure.exception.message.TechnicalErrorMessage;
import com.devsu.backendbank.infrastructure.input.dto.report.ReporteResponse;
import com.devsu.backendbank.infrastructure.input.mapper.ReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
@Tag(name = "Reportes")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApi reportApi;
    private final ReportMapper reportMapper;

    @GetMapping
    @Operation(summary = "Generar reporte por cliente y rango de fechas")
    public ResponseEntity<?> generate(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "json") String formato
    ) {
        if ("pdf".equalsIgnoreCase(formato)) {
            byte[] pdf = reportApi.generateReportPdf(clienteId, fechaDesde, fechaHasta);
            String fileName = "reporte-cliente-" + clienteId + "-" + fechaDesde + "-" + fechaHasta + ".pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentLength(pdf.length)
                    .body(pdf);
        }

        if (!"json".equalsIgnoreCase(formato)) {
            throw new TechnicalException(TechnicalErrorMessage.ILLEGAL_ARGUMENT);
        }

        ReporteResponse response = reportMapper.toResponse(reportApi.generateReport(clienteId, fechaDesde, fechaHasta));
        return ResponseEntity.ok(response);
    }
}
