package com.clinica.clinica.admision.dto;

import jakarta.validation.constraints.NotNull;

public record CuentaRequest(
    @NotNull Long pacienteId,
    Long medicoId,
    @NotNull Long tipoSeguroId,
    Long paqueteId,
    String observaciones
) {}
