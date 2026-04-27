package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import com.devsu.backendbank.infrastructure.output.repository.entity.AccountType;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import org.junit.jupiter.api.Nested;
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

import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.CLIENT_ID;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.FECHA_DESDE;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.FECHA_HASTA;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.currentClient;
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

    @Nested
    class GenerateReport {

        @Test
        void shouldCalculateTotalsForReportData() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(currentClient()));

            TransactionReportProjection debit = movement(
                    LocalDateTime.of(2026, 4, 2, 10, 15),
                    AccountType.AHORROS,
                    new BigDecimal("-300.00"),
                    new BigDecimal("1700.00")
            );
            TransactionReportProjection credit = movement(
                    LocalDateTime.of(2026, 4, 3, 8, 0),
                    AccountType.AHORROS,
                    new BigDecimal("500.00"),
                    new BigDecimal("2200.00")
            );

            when(bankDb.findReportByClientAndDateRange(eq(CLIENT_ID), any(LocalDateTime.class), any(LocalDateTime.class), eq(Pageable.unpaged())))
                    .thenReturn(new PageImpl<>(List.of(debit, credit)));

            var report = reportService.generateReport(CLIENT_ID, FECHA_DESDE, FECHA_HASTA);

            assertThat(report.clientId()).isEqualTo(CLIENT_ID);
            assertThat(report.totalDebitos()).isEqualByComparingTo("300.00");
            assertThat(report.totalCreditos()).isEqualByComparingTo("500.00");
            assertThat(report.items()).hasSize(2);
        }

        @Test
        void shouldReturnEmptyReportWhenThereAreNoMovements() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(currentClient()));
            when(bankDb.findReportByClientAndDateRange(eq(CLIENT_ID), any(LocalDateTime.class), any(LocalDateTime.class), eq(Pageable.unpaged())))
                    .thenReturn(new PageImpl<>(List.of()));

            var report = reportService.generateReport(CLIENT_ID, FECHA_DESDE, FECHA_HASTA);

            assertThat(report.totalDebitos()).isEqualByComparingTo("0.00");
            assertThat(report.totalCreditos()).isEqualByComparingTo("0.00");
            assertThat(report.items()).isEmpty();
        }

        @Test
        void shouldFailWhenDateRangeIsInvalid() {
            assertBusinessException(
                    () -> reportService.generateReport(CLIENT_ID, FECHA_HASTA, FECHA_DESDE),
                    BusinessErrorMessage.INVALID_DATE_RANGE
            );
        }

        @Test
        void shouldFailWhenClientDoesNotExist() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

            assertBusinessException(
                    () -> reportService.generateReport(CLIENT_ID, FECHA_DESDE, FECHA_HASTA),
                    BusinessErrorMessage.CLIENT_NOT_FOUND
            );
        }
    }

    @Nested
    class GeneratePdf {

        @Test
        void shouldGenerateValidPdfWithMovements() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(currentClient()));
            TransactionReportProjection projection = movement(
                    LocalDateTime.of(2026, 4, 2, 10, 15),
                    AccountType.CORRIENTE,
                    new BigDecimal("300.00"),
                    new BigDecimal("2300.00")
            );
            when(bankDb.findReportByClientAndDateRange(eq(CLIENT_ID), any(LocalDateTime.class), any(LocalDateTime.class), eq(Pageable.unpaged())))
                    .thenReturn(new PageImpl<>(List.of(projection)));

            byte[] pdf = reportService.generateReportPdf(CLIENT_ID, FECHA_DESDE, FECHA_HASTA);

            assertThat(pdf).isNotEmpty();
            assertThat(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
        }

        @Test
        void shouldGenerateValidPdfWhenThereAreNoMovements() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(currentClient()));
            when(bankDb.findReportByClientAndDateRange(eq(CLIENT_ID), any(LocalDateTime.class), any(LocalDateTime.class), eq(Pageable.unpaged())))
                    .thenReturn(new PageImpl<>(List.of()));

            byte[] pdf = reportService.generateReportPdf(CLIENT_ID, FECHA_DESDE, FECHA_HASTA);

            assertThat(pdf).isNotEmpty();
            assertThat(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
        }
    }

    private TransactionReportProjection movement(LocalDateTime fecha,
                                                 AccountType tipoCuenta,
                                                 BigDecimal movimiento,
                                                 BigDecimal saldoDisponible) {
        TransactionReportProjection projection = mock(TransactionReportProjection.class);
        when(projection.getFecha()).thenReturn(fecha);
        when(projection.getCliente()).thenReturn("Jose Castillo");
        when(projection.getNumeroCuenta()).thenReturn("478758");
        when(projection.getTipoCuenta()).thenReturn(tipoCuenta);
        when(projection.getSaldoInicial()).thenReturn(new BigDecimal("2000.00"));
        when(projection.getEstadoCuenta()).thenReturn(true);
        when(projection.getMovimiento()).thenReturn(movimiento);
        when(projection.getSaldoDisponible()).thenReturn(saldoDisponible);
        return projection;
    }

    private void assertBusinessException(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
                                         BusinessErrorMessage expectedMessage) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting(throwable -> ((BusinessException) throwable).getBusinessErrorMessage())
                .isEqualTo(expectedMessage);
    }
}


