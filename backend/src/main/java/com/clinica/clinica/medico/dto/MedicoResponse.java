package com.clinica.clinica.medico.dto;

import com.clinica.clinica.medico.entity.Medico;

public record MedicoResponse(
    Long id,
    Long personaId,
    Long trabajadorId,
    String trabajadorCodigo,
    String cmp,
    Long especialidadId,
    Boolean esEspecialista,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "MedicoResponse{id=" + id
            + ", personaId=" + personaId
            + ", trabajadorId=" + trabajadorId
            + ", cmp=" + cmp
            + ", especialidadId=" + especialidadId
            + ", esEspecialista=" + esEspecialista
            + ", activo=" + activo
            + "}";
    }

    public static MedicoResponse fromEntity(Medico entity) {
        return new MedicoResponse(
            entity.getId(),
            entity.getPersona() != null ? entity.getPersona().getId() : null,
            entity.getTrabajador() != null ? entity.getTrabajador().getId() : null,
            entity.getTrabajador() != null ? entity.getTrabajador().getCodigoTrabajador() : null,
            entity.getCmp(),
            entity.getEspecialidadId(),
            entity.getEsEspecialista(),
            entity.getActivo()
        );
    }
}
