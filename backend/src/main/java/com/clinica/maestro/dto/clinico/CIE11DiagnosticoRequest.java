package com.clinica.maestro.dto.clinico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CIE11DiagnosticoRequest(
    @NotBlank @Size(max = 8) String codigo,
    @NotBlank @Size(max = 500) String descripcion,
    @NotBlank @Size(max = 1) String categoria,
    @Size(max = 5) String sexoAplicable,
    Integer edadMinima,
    Integer edadMaxima,
    @Size(max = 10) String version
) {}
