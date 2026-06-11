package com.clinica.maestro.dto.clinico;

import com.clinica.maestro.entity.clinico.TipoAtencion;

public record TipoAtencionResponse(
    Long id,
    String codigo,
    String nombre,
    Boolean requiereHabitacion,
    Boolean activo
) {
    public static TipoAtencionResponse fromEntity(TipoAtencion entity) {
        return new TipoAtencionResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getRequiereHabitacion(),
            entity.getActivo()
        );
    }
}
