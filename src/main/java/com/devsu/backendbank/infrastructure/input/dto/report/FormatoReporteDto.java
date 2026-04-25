package com.devsu.backendbank.infrastructure.input.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FormatoReporteDto {
    @JsonProperty("json")
    JSON,
    @JsonProperty("pdf")
    PDF
}
