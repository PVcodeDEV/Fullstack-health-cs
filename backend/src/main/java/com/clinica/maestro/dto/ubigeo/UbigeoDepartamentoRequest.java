package com.clinica.maestro.dto.ubigeo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UbigeoDepartamentoRequest(
    @NotBlank @Size(min = 2, max = 2) String codigo,
    @NotBlank @Size(max = 100) String nombre
) {}
