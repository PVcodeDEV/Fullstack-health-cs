package com.clinica.caja.liquidacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Single payment method leg in a payment request.
 */
public record PagoLegRequest(

    @NotBlank(message = "El método de pago es obligatorio")
    String metodoPago,

    @NotNull(message = "El monto del pago es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    BigDecimal monto,

    String referencia
) {}
