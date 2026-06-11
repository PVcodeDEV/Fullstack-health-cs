package com.clinica.maestro.dto.clinico;

import com.clinica.maestro.entity.clinico.FormaFarmaceutica;

public record FormaFarmaceuticaResponse(
    Long id,
    String codigo,
    String nombre,
    Boolean requierePreparacion,
    Boolean activo
) {
    public static FormaFarmaceuticaResponse fromEntity(FormaFarmaceutica entity) {
        return new FormaFarmaceuticaResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getRequierePreparacion(),
            entity.getActivo()
        );
    }
}
