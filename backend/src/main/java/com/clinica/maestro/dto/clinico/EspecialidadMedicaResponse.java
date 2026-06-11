package com.clinica.maestro.dto.clinico;

import com.clinica.maestro.entity.clinico.EspecialidadMedica;

public record EspecialidadMedicaResponse(
    Long id,
    String codigo,
    String nombre,
    String abreviatura,
    Boolean activo
) {
    public static EspecialidadMedicaResponse fromEntity(EspecialidadMedica entity) {
        return new EspecialidadMedicaResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getAbreviatura(),
            entity.getActivo()
        );
    }
}
