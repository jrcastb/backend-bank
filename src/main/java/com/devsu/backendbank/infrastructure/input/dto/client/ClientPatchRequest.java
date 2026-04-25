package com.devsu.backendbank.infrastructure.input.dto.client;

import com.devsu.backendbank.infrastructure.input.dto.common.GenderDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ClientPatchRequest(
        @Size(max = 120) String nombre,
        GenderDto genero,
        @Min(0) @Max(130) Integer edad,
        @Size(max = 50) String identificacion,
        @Size(max = 180) String direccion,
        @Size(max = 30) String telefono,
        @Size(min = 8, max = 255) String contrasena,
        Boolean estado
) {
}

