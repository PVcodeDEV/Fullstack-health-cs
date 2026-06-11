package com.clinica.clinica.hospitalizacion.dto;

import java.time.LocalDateTime;

public record AltaMedicaResponse(
    Long id,
    Long hospitalizacionId,
    LocalDateTime fechaAlta,
    String tipoAlta,
    String diagnosticoFinal,
    String medicoNombre,
    String observaciones
) {
    @Override
    public final String toString() {
        return "AltaMedicaResponse{id=" + id + ", tipo=" + tipoAlta + "}";
    }
}
