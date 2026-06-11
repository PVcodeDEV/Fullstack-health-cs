package com.clinica.rrhh.derechohabiente.dto;

import com.clinica.rrhh.derechohabiente.entity.Derechohabiente;

import java.time.LocalDate;

public record DerechohabienteResponse(
    Long id,
    Long trabajadorId,
    Long personaId,
    String personaNombres,
    String personaNumeroDocumento,
    String relacion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String estado,
    Boolean activo
) {
    @Override public final String toString() {
        return "DerechohabienteResponse{id=" + id + ", relacion=" + relacion + ", estado=" + estado + "}";
    }

    public static DerechohabienteResponse fromEntity(Derechohabiente entity) {
        return new DerechohabienteResponse(
            entity.getId(),
            entity.getTrabajador().getId(),
            entity.getPersona().getId(),
            entity.getPersona().getNombres() + " " + entity.getPersona().getApellidoPaterno(),
            entity.getPersona().getNumeroDocumento(),
            entity.getRelacion().name(),
            entity.getFechaInicio(),
            entity.getFechaFin(),
            entity.getEstado(),
            entity.getActivo()
        );
    }
}
