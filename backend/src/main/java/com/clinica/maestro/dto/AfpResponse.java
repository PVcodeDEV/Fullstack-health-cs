package com.clinica.maestro.dto;

import com.clinica.maestro.entity.rrhh.Afp;

public record AfpResponse(
    Long id,
    String codigo,
    String nombre
) {
    public static AfpResponse fromEntity(Afp afp) {
        return new AfpResponse(
            afp.getId(),
            afp.getCodigo(),
            afp.getNombre()
        );
    }
}
