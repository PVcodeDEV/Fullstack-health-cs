package com.clinica.clinica.hospitalizacion.dto;

import java.time.LocalDate;

public record SolicitudMedicamentoResponse(
    Long id,
    Long hospitalizacionId,
    Long medicamentoId,
    String medicamentoNombre,
    String dosis,
    String frecuencia,
    Long viaAdministracionId,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String estado,
    String usuarioNombre
) {
    @Override
    public final String toString() {
        return "SolicitudMedicamentoResponse{id=" + id + ", medicamento=" + medicamentoNombre + ", estado=" + estado + "}";
    }
}
