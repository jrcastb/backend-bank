package com.devsu.backendbank.infrastructure.input.dto.common;

import com.devsu.backendbank.domain.enums.GenderDomain;

public enum GenderDto {
    MASCULINO,
    FEMENINO,
    OTRO;

    public GenderDomain toDomain() {
        return switch (this) {
            case MASCULINO -> GenderDomain.MASCULINO;
            case FEMENINO -> GenderDomain.FEMENINO;
            case OTRO -> GenderDomain.OTRO;
        };
    }

    public static GenderDto fromDomain(GenderDomain domain) {
        return switch (domain) {
            case MASCULINO -> MASCULINO;
            case FEMENINO -> FEMENINO;
            case OTRO -> OTRO;
        };
    }
}
