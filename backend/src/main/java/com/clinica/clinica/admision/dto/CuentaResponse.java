package com.clinica.clinica.admision.dto;

import java.time.LocalDateTime;

public record CuentaResponse(
    Long id,
    Long pacienteId,
    String pacienteNombre,
    Long medicoId,
    String medicoNombre,
    String tipoSeguro,
    String nroHistoriaClinica,
    LocalDateTime fechaAdmision,
    String estado,
    String observaciones
) {
    @Override
    public final String toString() {
        return "CuentaResponse{id=" + id + ", paciente=" + pacienteNombre + ", estado=" + estado + "}";
    }
}
