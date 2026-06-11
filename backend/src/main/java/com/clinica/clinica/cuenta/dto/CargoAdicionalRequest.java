package com.clinica.clinica.cuenta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CargoAdicionalRequest(
    @NotNull Long hospitalizacionId,
    @NotBlank String descripcion,
    @NotNull BigDecimal monto,
    String tipoCargo
) {}
