package com.clinica.maestro.dto.financiero;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoComprobanteRequest(
    @NotBlank @Size(max = 2) String codigoSunat,
    @NotBlank @Size(max = 100) String nombre
) {}
