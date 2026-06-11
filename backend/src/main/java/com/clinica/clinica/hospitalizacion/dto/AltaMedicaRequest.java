package com.clinica.clinica.hospitalizacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AltaMedicaRequest(
    @NotBlank String tipoAlta,
    @NotBlank String diagnosticoFinal,
    String observaciones,
    @NotNull Long medicoId
) {}
