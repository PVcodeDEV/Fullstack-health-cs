package com.clinica.maestro.dto.financiero;

import com.clinica.maestro.entity.financiero.TipoMoneda;

public record TipoMonedaResponse(
    Integer id,
    String codigoSunat,
    String nombre,
    String simbolo,
    Boolean activo
) {
    public static TipoMonedaResponse fromEntity(TipoMoneda entity) {
        return new TipoMonedaResponse(
            entity.getId(),
            entity.getCodigoSunat(),
            entity.getNombre(),
            entity.getSimbolo(),
            entity.getActivo()
        );
    }
}
