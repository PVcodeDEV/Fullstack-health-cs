package com.clinica.maestro.dto.clinico;

import com.clinica.maestro.entity.clinico.TipoPaciente;

public record TipoPacienteResponse(
    Long id,
    String codigo,
    String nombre,
    Boolean activo
) {
    public static TipoPacienteResponse fromEntity(TipoPaciente entity) {
        return new TipoPacienteResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getActivo()
        );
    }
}
