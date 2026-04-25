package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.input.port.ReportApi;
import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.domain.model.ReportItemDomain;
import com.devsu.backendbank.domain.model.ReportResultDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.TechnicalException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import com.devsu.backendbank.infrastructure.exception.message.TechnicalErrorMessage;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService implements ReportApi {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    public static final Integer MARGIN_LR = 24;
    public static final Integer MARGIN_TB = 20;

    private final BankDb bankDb;

    @Override
    public ReportResultDomain generateReport(Long clientId, LocalDate fechaDesde, LocalDate fechaHasta) {
        return buildReportData(clientId, fechaDesde, fechaHasta);
    }

    @Override
    public byte[] generateReportPdf(Long clientId, LocalDate fechaDesde, LocalDate fechaHasta) {
        ReportResultDomain reportData = buildReportData(clientId, fechaDesde, fechaHasta);
        return renderPdf(reportData);
    }

    private ReportResultDomain buildReportData(Long clientId, LocalDate fechaDesde, LocalDate fechaHasta) {
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

        return new ReportResultDomain(
                clientId,
                fechaDesde,
                fechaHasta,
                totalDebitos,
                totalCreditos,
                items
        );
    }

    private byte[] renderPdf(ReportResultDomain reportData) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), MARGIN_LR, MARGIN_LR, MARGIN_TB, MARGIN_TB);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Paragraph title = new Paragraph("Reporte de Estado de Cuenta", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12);
            document.add(title);

            document.add(new Paragraph(
                    "Cliente ID: " + reportData.clientId() +
                            "    |    Rango: " + reportData.fechaDesde() + " a " + reportData.fechaHasta(),
                    NORMAL_FONT));
            document.add(new Paragraph(
                    "Total creditos: " + formatAmount(reportData.totalCreditos()) +
                            "    |    Total debitos: " + formatAmount(reportData.totalDebitos()),
                    NORMAL_FONT));
            document.add(new Paragraph(" "));

            if (reportData.items().isEmpty()) {
                document.add(new Paragraph("No existen movimientos para el rango consultado.", SUBTITLE_FONT));
            } else {
                PdfPTable table = new PdfPTable(new float[]{2.3f, 2.5f, 1.6f, 1.4f, 1.4f, 1.0f, 1.4f, 1.5f});
                table.setWidthPercentage(100);
                table.setHeaderRows(1);

                addHeaderCell(table, "Fecha");
                addHeaderCell(table, "Cliente");
                addHeaderCell(table, "Cuenta");
                addHeaderCell(table, "Tipo");
                addHeaderCell(table, "Saldo Inicial");
                addHeaderCell(table, "Estado");
                addHeaderCell(table, "Movimiento");
                addHeaderCell(table, "Saldo Disp.");

                for (ReportItemDomain item : reportData.items()) {
                    addBodyCell(table, item.fecha() != null ? item.fecha().format(DATE_TIME_FORMATTER) : "-");
                    addBodyCell(table, defaultText(item.cliente()));
                    addBodyCell(table, defaultText(item.numeroCuenta()));
                    addBodyCell(table, defaultText(item.tipo()));
                    addBodyCell(table, formatAmount(item.saldoInicial()));
                    addBodyCell(table, Boolean.TRUE.equals(item.estado()) ? "ACTIVA" : "INACTIVA");
                    addBodyCell(table, formatAmount(item.movimiento()));
                    addBodyCell(table, formatAmount(item.saldoDisponible()));
                }

                document.add(table);
            }

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new TechnicalException(e, TechnicalErrorMessage.FILE_GENERATION_ERROR);
        }
    }

    private void addHeaderCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String formatAmount(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
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
