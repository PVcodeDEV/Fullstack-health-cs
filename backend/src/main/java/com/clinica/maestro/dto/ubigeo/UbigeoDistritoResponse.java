package com.clinica.maestro.dto.ubigeo;

import com.clinica.maestro.entity.ubigeo.UbigeoDistrito;

public record UbigeoDistritoResponse(
    String codigo,
    String nombre,
    String provinciaCodigo,
    Boolean activo
) {
    public static UbigeoDistritoResponse fromEntity(UbigeoDistrito entity) {
        return new UbigeoDistritoResponse(
            entity.getCodigo(),
            entity.getNombre(),
            entity.getProvincia().getCodigo(),
            entity.getActivo()
        );
    }
}
