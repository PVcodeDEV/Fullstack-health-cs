package com.clinica.clinica.hospitalizacion.dto;

import java.time.LocalDateTime;

public record NotaEvolucionResponse(
    Long id,
    Long hospitalizacionId,
    LocalDateTime fechaHora,
    String usuarioNombre,
    String descripcion,
    String plan,
    String tipo,
    String signosVitales
) {
    @Override
    public final String toString() {
        return "NotaEvolucionResponse{id=" + id + ", tipo=" + tipo + "}";
    }
}
