package com.clinica.seguridad.dto;

import com.clinica.seguridad.entity.NumeracionControl;

import java.time.LocalDateTime;

public record NumeracionControlResponse(
    Long id,
    String entidad,
    String serie,
    Long correlativoActual,
    String prefijo,
    int longitudCorrelativo,
    int anio,
    Boolean activo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    @Override
    public final String toString() {
        return "NumeracionControlResponse{id=" + id
            + ", entidad=" + entidad
            + ", serie=" + serie
            + ", anio=" + anio
            + ", activo=" + activo
            + "}";
    }

    public static NumeracionControlResponse fromEntity(NumeracionControl entity) {
        return new NumeracionControlResponse(
            entity.getId(),
            entity.getEntidad(),
            entity.getSerie(),
            entity.getCorrelativoActual(),
            entity.getPrefijo(),
            entity.getLongitudCorrelativo(),
            entity.getAnio(),
            entity.getActivo(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
