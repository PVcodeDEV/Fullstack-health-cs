package com.clinica.maestro.dto.clinico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FormaFarmaceuticaRequest(
    @NotBlank @Size(max = 10) String codigo,
    @NotBlank @Size(max = 100) String nombre,
    Boolean requierePreparacion
) {}
