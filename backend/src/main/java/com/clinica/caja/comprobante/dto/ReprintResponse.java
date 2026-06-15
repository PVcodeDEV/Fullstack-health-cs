package com.clinica.caja.comprobante.dto;

import com.clinica.caja.comprobante.entity.Comprobante;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reprint response with watermarked XML.
 * The XML content is prefixed with "COPIA / REIMPRESIÓN" notation
 * per CPR-004 requirement.
 */
public record ReprintResponse(
    Long id,
    String serieCorrelativo,
    Integer tipoComprobanteId,
    String estado,
    String nombreCliente,
    BigDecimal total,
    LocalDateTime fechaEmision,
    String xmlConCopia,
    Long reprintLogId,
    LocalDateTime reprintFecha
) {

    public static ReprintResponse fromEntity(Comprobante entity,
                                             String watermarkedXml,
                                             Long logId,
                                             LocalDateTime logFecha) {
        return new ReprintResponse(
            entity.getId(),
            entity.getSerie() + "-" + entity.getCorrelativo(),
            entity.getTipoComprobanteId(),
            entity.getEstado(),
            entity.getNombreCliente(),
            entity.getTotal(),
            entity.getFechaEmision(),
            watermarkedXml,
            logId,
            logFecha
        );
    }

    @Override
    public final String toString() {
        return "ReprintResponse{id=" + id + ", serieCorrelativo=" + serieCorrelativo
            + ", reprintLogId=" + reprintLogId + "}";
    }
}
