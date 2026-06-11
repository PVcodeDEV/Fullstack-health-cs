package com.clinica.rrhh.vacacion.dto;

import com.clinica.rrhh.vacacion.entity.VacacionRecord;
import java.math.BigDecimal;
import java.time.LocalDate;

public record VacacionRecordResponse(
    Long id, Long trabajadorId, String trabajadorNombre,
    Long contratoId,
    LocalDate fechaInicio, LocalDate fechaFin,
    Integer diasDerecho, Integer diasReduccion, BigDecimal diasPendientes,
    String estado, LocalDate fechaExpiracion
) {
    @Override public final String toString() {
        return "VacacionRecordResponse{id=" + id + ", trabajadorId=" + trabajadorId + ", estado=" + estado + "}";
    }
    public static VacacionRecordResponse fromEntity(VacacionRecord e) {
        return new VacacionRecordResponse(
            e.getId(), e.getTrabajador().getId(),
            e.getTrabajador().getPersona().getNombres() + " " + e.getTrabajador().getPersona().getApellidoPaterno(),
            e.getContrato() != null ? e.getContrato().getId() : null,
            e.getFechaInicio(), e.getFechaFin(),
            e.getDiasDerecho(), e.getDiasReduccion(), e.getDiasPendientes(),
            e.getEstado(), e.getFechaExpiracion());
    }
}
