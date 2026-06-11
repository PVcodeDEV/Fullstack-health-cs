package com.clinica.maestro.dto.identidad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoDocumentoIdentidadRequest(
    @NotBlank @Size(max = 5) String codigoSunat,
    @NotBlank @Size(max = 100) String nombre,
    @NotNull Integer longitudMinima,
    @NotNull Integer longitudMaxima
) {}
