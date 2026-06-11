package com.clinica.farmacia.venta.dto;

import com.clinica.farmacia.venta.entity.Venta;
import com.clinica.farmacia.venta.type.EstadoVenta;
import com.clinica.farmacia.venta.type.TipoLista;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
    Long id,
    Long sesionCajaId,
    Integer correlativo,
    Long clientePersonaId,
    TipoLista tipoLista,
    BigDecimal subtotal,
    BigDecimal descuentoTotal,
    BigDecimal impuesto,
    BigDecimal total,
    EstadoVenta estado,
    Boolean conImpresion,
    Long vendedorUsuarioId,
    String observaciones,
    LocalDateTime createdAt,
    List<DetalleVentaResponse> detalles
) {
    public static VentaResponse fromEntity(Venta entity) {
        return new VentaResponse(
            entity.getId(),
            entity.getSesionCaja().getId(),
            entity.getCorrelativo(),
            entity.getClientePersonaId(),
            entity.getTipoLista(),
            entity.getSubtotal(),
            entity.getDescuentoTotal(),
            entity.getImpuesto(),
            entity.getTotal(),
            entity.getEstado(),
            entity.getConImpresion(),
            entity.getVendedorUsuarioId(),
            entity.getObservaciones(),
            entity.getCreatedAt(),
            entity.getDetalles().stream()
                .map(DetalleVentaResponse::fromEntity)
                .toList()
        );
    }
}
