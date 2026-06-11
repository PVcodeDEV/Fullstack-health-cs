package com.clinica.maestro.dto.clinico;

import com.clinica.maestro.entity.clinico.ViaAdministracion;

public record ViaAdministracionResponse(
    Long id,
    String codigo,
    String nombre,
    Boolean activo
) {
    public static ViaAdministracionResponse fromEntity(ViaAdministracion entity) {
        return new ViaAdministracionResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getActivo()
        );
    }
}
