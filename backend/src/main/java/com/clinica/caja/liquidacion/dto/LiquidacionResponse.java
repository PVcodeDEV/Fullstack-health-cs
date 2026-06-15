package com.clinica.caja.liquidacion.dto;

import com.clinica.caja.liquidacion.entity.Liquidacion;
import com.clinica.caja.liquidacion.entity.PaymentLeg;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Liquidacion response after successful payment processing.
 */
public record LiquidacionResponse(
    Long id,
    Long cuentaId,
    Long sesionId,
    LocalDateTime fecha,
    String moneda,
    BigDecimal montoTotal,
    BigDecimal montoUSD,
    BigDecimal montoPEN,
    Long tipoCambioId,
    BigDecimal descuentoTotal,
    String estado,
    List<PagoLegResponse> pagos,
    LocalDateTime createdAt
) {

    public static LiquidacionResponse fromEntity(Liquidacion entity, List<PaymentLeg> legs) {
        List<PagoLegResponse> pagoLegs = legs.stream()
            .map(leg -> new PagoLegResponse(
                leg.getId(), leg.getMetodoPago(), leg.getMonto(), leg.getReferencia()))
            .toList();

        return new LiquidacionResponse(
            entity.getId(),
            entity.getCuentaId(),
            entity.getSesionId(),
            entity.getFecha(),
            entity.getMoneda(),
            entity.getMontoTotal(),
            entity.getMontoUSD(),
            entity.getMontoPEN(),
            entity.getTipoCambioId(),
            entity.getDescuentoTotal(),
            entity.getEstado(),
            pagoLegs,
            entity.getCreatedAt()
        );
    }

    @Override
    public final String toString() {
        return "LiquidacionResponse{id=" + id + ", cuentaId=" + cuentaId + ", estado=" + estado + "}";
    }
}
