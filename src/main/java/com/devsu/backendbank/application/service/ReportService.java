package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.input.port.ReportApi;
import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.domain.model.ReportItemDomain;
import com.devsu.backendbank.domain.model.ReportResultDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService implements ReportApi {

    private final BankDb bankDb;

    @Override
    public ReportResultDomain generateReport(Long clientId, LocalDate fechaDesde, LocalDate fechaHasta, String formato) {
        if (fechaDesde.isAfter(fechaHasta)) {
            throw new BusinessException(BusinessErrorMessage.INVALID_DATE_RANGE);
        }

        if (bankDb.findClientById(clientId).isEmpty()) {
            throw new BusinessException(BusinessErrorMessage.CLIENT_NOT_FOUND);
        }

        LocalDateTime from = fechaDesde.atStartOfDay();
        LocalDateTime to = fechaHasta.plusDays(1).atStartOfDay().minusNanos(1);

        List<ReportItemDomain> items = bankDb.findReportByClientAndDateRange(clientId, from, to, Pageable.unpaged())
                .stream()
                .map(this::toReportItem)
                .toList();

        BigDecimal totalDebitos = items.stream()
                .map(ReportItemDomain::movimiento)
                .filter(value -> value.signum() < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditos = items.stream()
                .map(ReportItemDomain::movimiento)
                .filter(value -> value.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String pdfBase64 = null;
        if ("pdf".equalsIgnoreCase(formato)) {
            String payload = "Reporte cliente=" + clientId + " desde=" + fechaDesde + " hasta=" + fechaHasta;
            pdfBase64 = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        }

        return new ReportResultDomain(
                clientId,
                fechaDesde,
                fechaHasta,
                totalDebitos,
                totalCreditos,
                items,
                pdfBase64
        );
    }

    private ReportItemDomain toReportItem(TransactionReportProjection projection) {
        return new ReportItemDomain(
                projection.getFecha(),
                projection.getCliente(),
                projection.getNumeroCuenta(),
                projection.getTipoCuenta().name(),
                projection.getSaldoInicial(),
                projection.getEstadoCuenta(),
                projection.getMovimiento(),
                projection.getSaldoDisponible()
        );
    }
}
