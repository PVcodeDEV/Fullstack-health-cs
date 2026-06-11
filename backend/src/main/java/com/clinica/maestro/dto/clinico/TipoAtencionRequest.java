package com.clinica.maestro.dto.clinico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoAtencionRequest(
    @NotBlank @Size(max = 10) String codigo,
    @NotBlank @Size(max = 100) String nombre,
    Boolean requiereHabitacion
) {}
