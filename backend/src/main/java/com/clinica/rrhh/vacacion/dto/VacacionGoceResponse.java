package com.clinica.rrhh.vacacion.dto;

import com.clinica.rrhh.vacacion.entity.VacacionGoce;
import java.math.BigDecimal;
import java.time.LocalDate;

public record VacacionGoceResponse(
    Long id, Long recordId,
    LocalDate fechaInicio, LocalDate fechaFin,
    Integer dias, BigDecimal remuneracion,
    String estado
) {
    @Override public final String toString() {
        return "VacacionGoceResponse{id=" + id + ", recordId=" + recordId + ", estado=" + estado + "}";
    }
    public static VacacionGoceResponse fromEntity(VacacionGoce e) {
        return new VacacionGoceResponse(
            e.getId(), e.getRecord().getId(),
            e.getFechaInicio(), e.getFechaFin(),
            e.getDias(), e.getRemuneracion(),
            e.getEstado());
    }
}
