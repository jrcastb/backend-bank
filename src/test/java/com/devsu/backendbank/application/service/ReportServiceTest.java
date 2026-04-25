package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.output.repository.entity.AccountType;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BankDb bankDb;

    @InjectMocks
    private ReportService reportService;

    @Test
    void shouldCalculateTotalsForReportData() {
        Long clientId = 1L;
        LocalDate fechaDesde = LocalDate.of(2026, 4, 1);
        LocalDate fechaHasta = LocalDate.of(2026, 4, 30);

        when(bankDb.findClientById(clientId)).thenReturn(Optional.of(mock(ClientDomain.class)));

        TransactionReportProjection debit = mock(TransactionReportProjection.class);
        when(debit.getFecha()).thenReturn(LocalDateTime.of(2026, 4, 2, 10, 15));
        when(debit.getCliente()).thenReturn("Jose Castillo");
        when(debit.getNumeroCuenta()).thenReturn("478758");
        when(debit.getTipoCuenta()).thenReturn(AccountType.AHORROS);
        when(debit.getSaldoInicial()).thenReturn(new BigDecimal("2000.00"));
        when(debit.getEstadoCuenta()).thenReturn(true);
        when(debit.getMovimiento()).thenReturn(new BigDecimal("-300.00"));
        when(debit.getSaldoDisponible()).thenReturn(new BigDecimal("1700.00"));

        TransactionReportProjection credit = mock(TransactionReportProjection.class);
        when(credit.getFecha()).thenReturn(LocalDateTime.of(2026, 4, 3, 8, 0));
        when(credit.getCliente()).thenReturn("Jose Castillo");
        when(credit.getNumeroCuenta()).thenReturn("478758");
        when(credit.getTipoCuenta()).thenReturn(AccountType.AHORROS);
        when(credit.getSaldoInicial()).thenReturn(new BigDecimal("2000.00"));
        when(credit.getEstadoCuenta()).thenReturn(true);
        when(credit.getMovimiento()).thenReturn(new BigDecimal("500.00"));
        when(credit.getSaldoDisponible()).thenReturn(new BigDecimal("2200.00"));

        when(bankDb.findReportByClientAndDateRange(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(debit, credit)));

        var report = reportService.generateReport(clientId, fechaDesde, fechaHasta);

        assertThat(report.totalDebitos()).isEqualByComparingTo("300.00");
        assertThat(report.totalCreditos()).isEqualByComparingTo("500.00");
        assertThat(report.items()).hasSize(2);
    }

    @Test
    void shouldGenerateValidPdf() {
        Long clientId = 1L;
        LocalDate fechaDesde = LocalDate.of(2026, 4, 1);
        LocalDate fechaHasta = LocalDate.of(2026, 4, 30);

        when(bankDb.findClientById(clientId)).thenReturn(Optional.of(mock(ClientDomain.class)));

        TransactionReportProjection movement = mock(TransactionReportProjection.class);
        when(movement.getFecha()).thenReturn(LocalDateTime.of(2026, 4, 2, 10, 15));
        when(movement.getCliente()).thenReturn("Jose Castillo");
        when(movement.getNumeroCuenta()).thenReturn("478758");
        when(movement.getTipoCuenta()).thenReturn(AccountType.CORRIENTE);
        when(movement.getSaldoInicial()).thenReturn(new BigDecimal("2000.00"));
        when(movement.getEstadoCuenta()).thenReturn(true);
        when(movement.getMovimiento()).thenReturn(new BigDecimal("300.00"));
        when(movement.getSaldoDisponible()).thenReturn(new BigDecimal("2300.00"));

        when(bankDb.findReportByClientAndDateRange(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(movement)));

        byte[] pdf = reportService.generateReportPdf(clientId, fechaDesde, fechaHasta);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    void shouldFailWhenDateRangeIsInvalid() {
        assertThatThrownBy(() -> reportService.generateReport(1L, LocalDate.of(2026, 4, 30), LocalDate.of(2026, 4, 1)))
                .isInstanceOf(BusinessException.class);
    }
}


