package com.devsu.backendbank.domain.model;

import java.time.LocalDateTime;

public record PersonDomain(
        Long id,
        String nombre,
        GenderDomain genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

