package com.clinica.maestro.dto.financiero;

import com.clinica.maestro.entity.financiero.TipoComprobante;

public record TipoComprobanteResponse(
    Integer id,
    String codigoSunat,
    String nombre,
    Boolean activo
) {
    public static TipoComprobanteResponse fromEntity(TipoComprobante entity) {
        return new TipoComprobanteResponse(
            entity.getId(),
            entity.getCodigoSunat(),
            entity.getNombre(),
            entity.getActivo()
        );
    }
}
