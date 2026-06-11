package com.clinica.clinica.paciente.dto;

import com.clinica.clinica.paciente.entity.Paciente;

public record PacienteResponse(
    Long id,
    Long personaId,
    String personaNombres,
    String personaApellidoPaterno,
    String personaNumeroDocumento,
    String tipoPaciente,
    String nroHistoriaClinica,
    String grupoSanguineo,
    String alergias,
    String contactoEmergenciaNombre,
    String contactoEmergenciaTelefono,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "PacienteResponse{id=" + id
            + ", personaId=" + personaId
            + ", tipoPaciente=" + tipoPaciente
            + ", nroHistoriaClinica=" + nroHistoriaClinica
            + ", grupoSanguineo=" + grupoSanguineo
            + ", activo=" + activo
            + "}";
    }

    public static PacienteResponse fromEntity(Paciente entity) {
        return new PacienteResponse(
            entity.getId(),
            entity.getPersona() != null ? entity.getPersona().getId() : null,
            entity.getPersona() != null ? entity.getPersona().getNombres() : null,
            entity.getPersona() != null ? entity.getPersona().getApellidoPaterno() : null,
            entity.getPersona() != null ? entity.getPersona().getNumeroDocumento() : null,
            entity.getTipoPaciente(),
            entity.getNroHistoriaClinica(),
            entity.getGrupoSanguineo(),
            entity.getAlergias(),
            entity.getContactoEmergenciaNombre(),
            entity.getContactoEmergenciaTelefono(),
            entity.getActivo()
        );
    }
}
