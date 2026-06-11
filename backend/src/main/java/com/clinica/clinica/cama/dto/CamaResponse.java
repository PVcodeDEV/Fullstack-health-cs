package com.clinica.clinica.cama.dto;

public record CamaResponse(
    Long id,
    Long habitacionId,
    String codigo,
    String estado,
    Boolean activo,
    String observaciones
) {
    @Override
    public final String toString() {
        return "CamaResponse{id=" + id + ", codigo=" + codigo + ", estado=" + estado + "}";
    }
}
