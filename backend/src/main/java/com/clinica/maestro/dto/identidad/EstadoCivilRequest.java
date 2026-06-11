package com.clinica.maestro.dto.identidad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadoCivilRequest(
    @NotBlank @Size(max = 3) String codigoReniec,
    @NotBlank @Size(max = 50) String nombre
) {}
