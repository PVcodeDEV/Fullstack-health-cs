package com.clinica.caja.liquidacion.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pre-liquidación preview response (read-only, not persisted).
 * Generated before payment for patient review.
 */
public record PreLiquidacionResponse(
    Long cuentaId,
    List<PreLiquidacionItem> items,
    BigDecimal subtotal,
    BigDecimal igv,
    BigDecimal total,
    String estado
) {
    public static PreLiquidacionResponse create(Long cuentaId, List<PreLiquidacionItem> items,
                                                 BigDecimal subtotal, BigDecimal igvPorcentaje) {
        BigDecimal igv = subtotal.multiply(igvPorcentaje.divide(BigDecimal.valueOf(100)));
        BigDecimal total = subtotal.add(igv);
        return new PreLiquidacionResponse(cuentaId, items, subtotal, igv, total, "PREVIEW");
    }

    @Override
    public final String toString() {
        return "PreLiquidacionResponse{cuentaId=" + cuentaId + ", estado=" + estado + "}";
    }
}
