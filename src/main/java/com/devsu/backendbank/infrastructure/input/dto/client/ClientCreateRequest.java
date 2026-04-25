package com.devsu.backendbank.infrastructure.input.dto.client;

import com.devsu.backendbank.infrastructure.input.dto.common.GenderDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientCreateRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotNull GenderDto genero,
        @NotNull @Min(0) @Max(130) Integer edad,
        @NotBlank @Size(max = 50) String identificacion,
        @NotBlank @Size(max = 180) String direccion,
        @NotBlank @Size(max = 30) String telefono,
        @NotBlank @Size(min = 8, max = 255) String contrasena,
        Boolean estado
) {
}

