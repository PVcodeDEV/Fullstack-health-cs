package com.clinica.caja.comprobante.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request to issue a Nota de Crédito against an existing comprobante.
 */
public record NotaCreditoRequest(

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    BigDecimal monto,

    @NotBlank(message = "El motivo es obligatorio")
    String motivo
) {}
