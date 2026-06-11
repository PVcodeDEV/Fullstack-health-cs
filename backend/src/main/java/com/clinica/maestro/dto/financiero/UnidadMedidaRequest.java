package com.clinica.maestro.dto.financiero;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnidadMedidaRequest(
    @NotBlank @Size(max = 5) String codigoSunat,
    @NotBlank @Size(max = 100) String nombre,
    @Size(max = 10) String abreviatura
) {}
