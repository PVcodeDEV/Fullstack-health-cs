package com.clinica.clinica.admision.dto;

import java.time.LocalDateTime;

public record SolicitudHospitalizacionResponse(
    Long id,
    Long cuentaId,
    String pacienteNombre,
    String tipoHabitacionSugerida,
    String estado,
    LocalDateTime fechaSolicitud
) {
    @Override
    public final String toString() {
        return "SolicitudHospitalizacionResponse{id=" + id + ", estado=" + estado + "}";
    }
}
