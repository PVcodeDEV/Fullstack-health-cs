package com.clinica.maestro.dto.ubigeo;

import com.clinica.maestro.entity.ubigeo.UbigeoDepartamento;

public record UbigeoDepartamentoResponse(
    String codigo,
    String nombre,
    Boolean activo
) {
    public static UbigeoDepartamentoResponse fromEntity(UbigeoDepartamento entity) {
        return new UbigeoDepartamentoResponse(
            entity.getCodigo(),
            entity.getNombre(),
            entity.getActivo()
        );
    }
}
