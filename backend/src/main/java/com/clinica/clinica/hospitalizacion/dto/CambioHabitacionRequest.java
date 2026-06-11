package com.clinica.clinica.hospitalizacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CambioHabitacionRequest(
    @NotNull Long camaDestinoId,
    @NotBlank String motivo
) {}
