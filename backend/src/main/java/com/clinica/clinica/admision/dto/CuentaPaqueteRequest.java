package com.clinica.clinica.admision.dto;

import jakarta.validation.constraints.NotNull;

public record CuentaPaqueteRequest(
    @NotNull Long cuentaId,
    @NotNull Long paqueteId
) {}
