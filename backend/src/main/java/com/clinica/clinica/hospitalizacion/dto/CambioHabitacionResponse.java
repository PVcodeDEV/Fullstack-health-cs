package com.clinica.clinica.hospitalizacion.dto;

import java.time.LocalDateTime;

public record CambioHabitacionResponse(
    Long id,
    Long hospitalizacionId,
    String camaOrigenCodigo,
    String camaDestinoCodigo,
    LocalDateTime fechaCambio,
    String motivo,
    String usuarioNombre
) {
    @Override
    public final String toString() {
        return "CambioHabitacionResponse{id=" + id + "}";
    }
}
