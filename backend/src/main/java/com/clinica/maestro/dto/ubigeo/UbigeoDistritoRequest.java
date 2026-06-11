package com.clinica.maestro.dto.ubigeo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UbigeoDistritoRequest(
    @NotBlank @Size(min = 6, max = 6) String codigo,
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Size(min = 4, max = 4) String provinciaCodigo
) {}
