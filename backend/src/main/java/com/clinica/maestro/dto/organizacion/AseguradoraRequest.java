package com.clinica.maestro.dto.organizacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AseguradoraRequest(
    @NotBlank @Size(max = 10) String codigo,
    @NotBlank @Size(max = 150) String nombre,
    @NotBlank @Size(max = 10) String tipo,
    @NotNull Boolean contratoVigente
) {}
