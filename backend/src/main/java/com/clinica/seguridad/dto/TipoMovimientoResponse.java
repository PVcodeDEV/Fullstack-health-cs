package com.clinica.seguridad.dto;

import com.clinica.seguridad.entity.TipoMovimiento;

import java.time.LocalDateTime;

public record TipoMovimientoResponse(
    Long id,
    String codigo,
    String nombre,
    String modulo,
    String descripcion,
    Boolean activo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    @Override
    public final String toString() {
        return "TipoMovimientoResponse{id=" + id
            + ", codigo=" + codigo
            + ", modulo=" + modulo
            + ", activo=" + activo
            + "}";
    }

    public static TipoMovimientoResponse fromEntity(TipoMovimiento entity) {
        return new TipoMovimientoResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getModulo(),
            entity.getDescripcion(),
            entity.getActivo(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
