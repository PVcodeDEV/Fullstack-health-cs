package com.clinica.clinica.admision.dto;

import jakarta.validation.constraints.NotBlank;

public record AdmisionDiagnosticoRequest(
    @NotBlank String codigoCie11,
    String tipoDiagnostico,
    String descripcion
) {}
