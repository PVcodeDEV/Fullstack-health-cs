package com.clinica.rrhh.planilla.dto;

import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import java.time.LocalDate;

public record PeriodoPlanillaResponse(
    Long id, Integer anio, Integer mes,
    LocalDate fechaInicio, LocalDate fechaFin,
    String estado, Boolean activo
) {
    @Override public final String toString() {
        return "PeriodoPlanillaResponse{id=" + id + ", periodo=" + anio + "-" + String.format("%02d", mes) + ", estado=" + estado + "}";
    }
    public static PeriodoPlanillaResponse fromEntity(PeriodoPlanilla e) {
        return new PeriodoPlanillaResponse(e.getId(), e.getAnio(), e.getMes(),
            e.getFechaInicio(), e.getFechaFin(), e.getEstado(), e.getActivo());
    }
}
