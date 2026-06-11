package com.clinica.maestro.dto.organizacion;

import com.clinica.maestro.entity.organizacion.Aseguradora;

public record AseguradoraResponse(
    Integer id,
    String codigo,
    String nombre,
    String tipo,
    Boolean contratoVigente,
    Boolean activo
) {
    public static AseguradoraResponse fromEntity(Aseguradora entity) {
        return new AseguradoraResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getTipo(),
            entity.getContratoVigente(),
            entity.getActivo()
        );
    }
}
