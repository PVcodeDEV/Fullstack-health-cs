package com.clinica.caja.sesion.dto;

import com.clinica.caja.sesion.entity.SesionCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionCajaResponse(
    Long id,
    String codigo,
    Long usuarioAperturaId,
    LocalDateTime fechaApertura,
    BigDecimal montoApertura,
    String estado,
    Long usuarioCierreId,
    LocalDateTime fechaCierre,
    BigDecimal montoCierre,
    BigDecimal totalVentas,
    BigDecimal diferencia,
    String observaciones,
    boolean discrepanciaWarning,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static SesionCajaResponse fromEntity(SesionCaja entity) {
        return fromEntity(entity, false);
    }

    public static SesionCajaResponse fromEntity(SesionCaja entity, boolean discrepanciaWarning) {
        return new SesionCajaResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getUsuarioAperturaId(),
            entity.getFechaApertura(),
            entity.getMontoApertura(),
            entity.getEstado().name(),
            entity.getUsuarioCierreId(),
            entity.getFechaCierre(),
            entity.getMontoCierre(),
            entity.getTotalVentas(),
            entity.getDiferencia(),
            entity.getObservaciones(),
            discrepanciaWarning,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
