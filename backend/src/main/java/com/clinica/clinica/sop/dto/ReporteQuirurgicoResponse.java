package com.clinica.clinica.sop.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReporteQuirurgicoResponse(
    Long id,
    Long hospitalizacionId,
    Long cirujanoId,
    String cirujanoNombre,
    Long anestesiologoId,
    String anestesiologoNombre,
    String diagnosticoPreoperatorio,
    String procedimientoRealizado,
    String hallazgos,
    String complicaciones,
    LocalDate fechaCirugia,
    LocalTime horaInicio,
    LocalTime horaFin,
    String estado,
    LocalDateTime createdAt
) {
    @Override
    public final String toString() {
        return "ReporteQuirurgicoResponse{id=" + id + ", procedimiento=" + procedimientoRealizado + ", estado=" + estado + "}";
    }
}
