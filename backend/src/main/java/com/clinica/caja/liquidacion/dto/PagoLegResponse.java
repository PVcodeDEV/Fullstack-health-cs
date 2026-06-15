package com.clinica.caja.liquidacion.dto;

import java.math.BigDecimal;

/**
 * Payment leg response after successful payment.
 */
public record PagoLegResponse(
    Long id,
    String metodoPago,
    BigDecimal monto,
    String referencia
) {
    @Override
    public final String toString() {
        return "PagoLegResponse{id=" + id + ", metodoPago=" + metodoPago + "}";
    }
}
