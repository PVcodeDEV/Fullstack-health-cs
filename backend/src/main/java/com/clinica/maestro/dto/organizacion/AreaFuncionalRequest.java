package com.clinica.maestro.dto.organizacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AreaFuncionalRequest(
    @NotBlank @Size(max = 10) String codigo,
    @NotBlank @Size(max = 100) String nombre,
    @NotNull Boolean esAreaFisica
) {}
