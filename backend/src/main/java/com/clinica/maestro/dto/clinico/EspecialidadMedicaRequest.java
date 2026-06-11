package com.clinica.maestro.dto.clinico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecialidadMedicaRequest(
    @NotBlank @Size(max = 10) String codigo,
    @NotBlank @Size(max = 100) String nombre,
    @Size(max = 10) String abreviatura
) {}
