package com.devsu.backendbank.infrastructure.input.mapper;

import com.devsu.backendbank.domain.model.ReportItemDomain;
import com.devsu.backendbank.domain.model.ReportResultDomain;
import com.devsu.backendbank.infrastructure.input.dto.report.ReporteItemResponse;
import com.devsu.backendbank.infrastructure.input.dto.report.ReporteResponse;
import com.devsu.backendbank.infrastructure.input.dto.report.ReporteResumenResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    ReporteItemResponse toItemResponse(ReportItemDomain domain);

    default ReporteResponse toResponse(ReportResultDomain domain) {
        return new ReporteResponse(
                domain.clientId(),
                domain.fechaDesde(),
                domain.fechaHasta(),
                new ReporteResumenResponse(domain.totalDebitos(), domain.totalCreditos()),
                domain.items().stream().map(this::toItemResponse).toList(),
                domain.pdfBase64()
        );
    }
}
