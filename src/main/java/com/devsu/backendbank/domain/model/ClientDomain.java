package com.devsu.backendbank.domain.model;

import java.time.LocalDateTime;

public record ClientDomain(
        Long id,
        PersonDomain person,
        String contrasena,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

