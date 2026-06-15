package com.clinica.caja.comprobante.dto;

import com.clinica.caja.comprobante.entity.Comprobante;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Comprobante response. The {@code xmlGenerado} field is only included
 * when explicitly requested (includeXml=true) per CPR-007 data privacy.
 */
public record ComprobanteResponse(
    Long id,
    Integer tipoComprobanteId,
    String serie,
    String correlativo,
    String serieCorrelativo,
    LocalDateTime fechaEmision,
    String tipoDocCliente,
    String numDocCliente,
    String nombreCliente,
    String direccionCliente,
    Long personaId,
    Long empresaId,
    BigDecimal subtotal,
    BigDecimal igv,
    BigDecimal total,
    String moneda,
    Long liquidacionId,
    String xmlGenerado,
    String estado,
    Long comprobanteOriginalId,
    String motivo,
    LocalDateTime createdAt
) {

    public static ComprobanteResponse fromEntity(Comprobante entity, boolean includeXml) {
        return new ComprobanteResponse(
            entity.getId(),
            entity.getTipoComprobanteId(),
            entity.getSerie(),
            entity.getCorrelativo(),
            entity.getSerie() + "-" + entity.getCorrelativo(),
            entity.getFechaEmision(),
            entity.getTipoDocCliente(),
            entity.getNumDocCliente(),
            entity.getNombreCliente(),
            entity.getDireccionCliente(),
            entity.getPersonaId(),
            entity.getEmpresaId(),
            entity.getSubtotal(),
            entity.getIgv(),
            entity.getTotal(),
            entity.getMoneda(),
            entity.getLiquidacionId(),
            includeXml ? entity.getXmlGenerado() : null,
            entity.getEstado(),
            entity.getComprobanteOriginalId(),
            entity.getMotivo(),
            entity.getCreatedAt()
        );
    }

    @Override
    public final String toString() {
        return "ComprobanteResponse{id=" + id + ", serieCorrelativo=" + serieCorrelativo
            + ", estado=" + estado + "}";
    }
}
