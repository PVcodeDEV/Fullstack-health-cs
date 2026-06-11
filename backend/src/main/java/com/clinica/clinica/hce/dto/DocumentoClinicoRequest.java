package com.clinica.clinica.hce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoClinicoRequest(
    @NotNull Long hospitalizacionId,
    @NotBlank String tipoDocumento,
    @NotBlank String contenido,
    String descripcion
) {}
