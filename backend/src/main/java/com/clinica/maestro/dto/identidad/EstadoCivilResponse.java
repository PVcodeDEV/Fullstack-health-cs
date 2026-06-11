package com.clinica.maestro.dto.identidad;

import com.clinica.maestro.entity.identidad.EstadoCivil;

public record EstadoCivilResponse(
    Long id,
    String codigoReniec,
    String nombre,
    Boolean activo
) {
    public static EstadoCivilResponse fromEntity(EstadoCivil entity) {
        return new EstadoCivilResponse(
            entity.getId(),
            entity.getCodigoReniec(),
            entity.getNombre(),
            entity.getActivo()
        );
    }
}
