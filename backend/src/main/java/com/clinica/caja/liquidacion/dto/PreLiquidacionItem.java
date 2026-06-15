package com.clinica.caja.liquidacion.dto;

import java.math.BigDecimal;

/**
 * Individual charge item in a pre-liquidación preview.
 */
public record PreLiquidacionItem(
    Long id,
    String tipo,
    String descripcion,
    BigDecimal monto
) {
    @Override
    public final String toString() {
        return "PreLiquidacionItem{id=" + id + ", tipo=" + tipo + "}";
    }
}
