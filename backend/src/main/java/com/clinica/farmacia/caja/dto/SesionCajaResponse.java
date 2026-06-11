package com.clinica.farmacia.caja.dto;

import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.type.EstadoSesion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionCajaResponse(
    Long id,
    Long usuarioId,
    Long almacenId,
    EstadoSesion estado,
    BigDecimal montoApertura,
    BigDecimal montoCierreEsperado,
    BigDecimal montoCierreReal,
    BigDecimal diferenciaCierre,
    BigDecimal totalVentas,
    LocalDateTime fechaApertura,
    LocalDateTime fechaCierre,
    String observacionesApertura,
    String observacionesCierre,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static SesionCajaResponse fromEntity(SesionCaja entity) {
        return new SesionCajaResponse(
            entity.getId(),
            entity.getUsuarioId(),
            entity.getAlmacenId(),
            entity.getEstado(),
            entity.getMontoApertura(),
            entity.getMontoCierreEsperado(),
            entity.getMontoCierreReal(),
            entity.getDiferenciaCierre(),
            entity.getTotalVentas(),
            entity.getFechaApertura(),
            entity.getFechaCierre(),
            entity.getObservacionesApertura(),
            entity.getObservacionesCierre(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
