package com.clinica.farmacia.venta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DetalleVentaRequest(

    @NotNull(message = "El lote es obligatorio")
    Long loteId,

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    Integer cantidad,

    @DecimalMin(value = "0.00", message = "El descuento manual no puede ser negativo")
    @Digits(integer = 10, fraction = 4, message = "El descuento manual debe tener máximo 10 enteros y 4 decimales")
    BigDecimal descuentoManual

) {
    public DetalleVentaRequest {
        if (descuentoManual == null) {
            descuentoManual = BigDecimal.ZERO;
        }
    }
}
