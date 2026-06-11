package com.clinica.maestro.dto.financiero;

import com.clinica.maestro.entity.financiero.UnidadMedida;

public record UnidadMedidaResponse(
    Integer id,
    String codigoSunat,
    String nombre,
    String abreviatura,
    Boolean activo
) {
    public static UnidadMedidaResponse fromEntity(UnidadMedida entity) {
        return new UnidadMedidaResponse(
            entity.getId(),
            entity.getCodigoSunat(),
            entity.getNombre(),
            entity.getAbreviatura(),
            entity.getActivo()
        );
    }
}
