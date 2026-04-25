package com.devsu.backendbank.infrastructure.input.dto.client;

import com.devsu.backendbank.infrastructure.input.dto.common.GenderDto;

public record ClientResponse(
        Long id,
        String nombre,
        GenderDto genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        Boolean estado
) {
}

