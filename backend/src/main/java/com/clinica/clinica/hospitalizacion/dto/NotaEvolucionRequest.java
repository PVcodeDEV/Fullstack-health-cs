package com.clinica.clinica.hospitalizacion.dto;

import jakarta.validation.constraints.NotBlank;

public record NotaEvolucionRequest(
    @NotBlank String descripcion,
    String plan,
    String tipo,
    String signosVitales
) {}
