package com.clinica.farmacia.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ActualizarUtilidadRequest(
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 3, fraction = 2)
    BigDecimal utilidadMedico,

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 3, fraction = 2)
    BigDecimal utilidadPublico
) {}
