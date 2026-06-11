package com.clinica.maestro.dto.organizacion;

import com.clinica.maestro.entity.organizacion.AreaFuncional;

public record AreaFuncionalResponse(
    Integer id,
    String codigo,
    String nombre,
    Boolean esAreaFisica,
    Boolean activo
) {
    public static AreaFuncionalResponse fromEntity(AreaFuncional entity) {
        return new AreaFuncionalResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getEsAreaFisica(),
            entity.getActivo()
        );
    }
}
