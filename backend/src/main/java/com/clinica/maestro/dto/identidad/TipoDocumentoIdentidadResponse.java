package com.clinica.maestro.dto.identidad;

import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;

public record TipoDocumentoIdentidadResponse(
    Long id,
    String codigoSunat,
    String nombre,
    Integer longitudMinima,
    Integer longitudMaxima,
    Boolean activo
) {
    public static TipoDocumentoIdentidadResponse fromEntity(TipoDocumentoIdentidad entity) {
        return new TipoDocumentoIdentidadResponse(
            entity.getId(),
            entity.getCodigoSunat(),
            entity.getNombre(),
            entity.getLongitudMinima(),
            entity.getLongitudMaxima(),
            entity.getActivo()
        );
    }
}
