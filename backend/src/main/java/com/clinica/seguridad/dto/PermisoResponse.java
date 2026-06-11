package com.clinica.seguridad.dto;

import com.clinica.seguridad.entity.Permiso;

public record PermisoResponse(
    Long id,
    String codigo,
    String nombre,
    String modulo,
    String descripcion,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "PermisoResponse{id=" + id
            + ", codigo=" + codigo
            + ", modulo=" + modulo
            + ", activo=" + activo
            + "}";
    }

    public static PermisoResponse fromEntity(Permiso entity) {
        return new PermisoResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getModulo(),
            entity.getDescripcion(),
            entity.getActivo()
        );
    }
}
