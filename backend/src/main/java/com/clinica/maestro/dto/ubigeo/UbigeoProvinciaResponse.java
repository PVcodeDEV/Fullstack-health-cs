package com.clinica.maestro.dto.ubigeo;

import com.clinica.maestro.entity.ubigeo.UbigeoProvincia;

public record UbigeoProvinciaResponse(
    String codigo,
    String nombre,
    String departamentoCodigo,
    Boolean activo
) {
    public static UbigeoProvinciaResponse fromEntity(UbigeoProvincia entity) {
        return new UbigeoProvinciaResponse(
            entity.getCodigo(),
            entity.getNombre(),
            entity.getDepartamento().getCodigo(),
            entity.getActivo()
        );
    }
}
