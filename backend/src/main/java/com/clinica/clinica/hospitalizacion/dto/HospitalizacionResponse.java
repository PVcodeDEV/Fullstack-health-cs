package com.clinica.clinica.hospitalizacion.dto;

import java.time.LocalDateTime;

public record HospitalizacionResponse(
    Long id,
    Long cuentaId,
    String pacienteNombre,
    String camaCodigo,
    LocalDateTime fechaIngreso,
    LocalDateTime fechaAlta,
    String estado
) {
    @Override
    public final String toString() {
        return "HospitalizacionResponse{id=" + id + ", paciente=" + pacienteNombre + ", estado=" + estado + "}";
    }
}
