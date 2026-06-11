package com.clinica.maestro.dto.clinico;

import com.clinica.maestro.entity.clinico.CIE11Diagnostico;

public record CIE11DiagnosticoResponse(
    Long id,
    String codigo,
    String descripcion,
    String categoria,
    String sexoAplicable,
    Integer edadMinima,
    Integer edadMaxima,
    String version,
    Integer frecuenciaUso
) {
    public static CIE11DiagnosticoResponse fromEntity(CIE11Diagnostico entity) {
        return new CIE11DiagnosticoResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getDescripcion(),
            entity.getCategoria(),
            entity.getSexoAplicable(),
            entity.getEdadMinima(),
            entity.getEdadMaxima(),
            entity.getVersion(),
            entity.getFrecuenciaUso()
        );
    }
}
