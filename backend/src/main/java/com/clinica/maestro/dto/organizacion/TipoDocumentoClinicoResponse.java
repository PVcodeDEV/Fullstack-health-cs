package com.clinica.maestro.dto.organizacion;

import com.clinica.maestro.entity.organizacion.TipoDocumentoClinico;

public record TipoDocumentoClinicoResponse(
    Integer id,
    String codigo,
    String nombre,
    Boolean requiereFirma,
    Boolean activo
) {
    public static TipoDocumentoClinicoResponse fromEntity(TipoDocumentoClinico entity) {
        return new TipoDocumentoClinicoResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getRequiereFirma(),
            entity.getActivo()
        );
    }
}
