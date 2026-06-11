package com.clinica.seguridad.dto;

import com.clinica.seguridad.entity.Rol;

import java.util.List;

public record RolResponse(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    Boolean activo,
    List<String> permisos
) {
    @Override
    public final String toString() {
        return "RolResponse{id=" + id
            + ", codigo=" + codigo
            + ", nombre=" + nombre
            + ", activo=" + activo
            + "}";
    }

    public static RolResponse fromEntity(Rol entity, List<String> permisos) {
        return new RolResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getDescripcion(),
            entity.getActivo(),
            permisos
        );
    }
}
