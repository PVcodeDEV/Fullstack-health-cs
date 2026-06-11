package com.clinica.farmacia.venta.dto;

import com.clinica.farmacia.venta.entity.DetalleVenta;

import java.math.BigDecimal;

public record DetalleVentaResponse(
    Long id,
    Long loteId,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal precioOriginal,
    BigDecimal descuentoAplicado,
    BigDecimal subtotal
) {
    public static DetalleVentaResponse fromEntity(DetalleVenta entity) {
        return new DetalleVentaResponse(
            entity.getId(),
            entity.getLote().getId(),
            entity.getCantidad(),
            entity.getPrecioUnitario(),
            entity.getPrecioOriginal(),
            entity.getDescuentoAplicado(),
            entity.getSubtotal()
        );
    }
}
