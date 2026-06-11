package com.clinica.clinica.cama.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CamaRequest(
    @NotNull Long habitacionId,
    @NotBlank String codigo,
    String observaciones
) {}
