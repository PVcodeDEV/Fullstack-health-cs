package com.clinica.clinica.hce.dto;

import java.time.LocalDateTime;

public record FirmaDigitalResponse(
    Long id,
    Long documentoId,
    String usuarioNombre,
    LocalDateTime fechaFirma,
    String hashSha256,
    String ipOrigen
) {
    @Override
    public final String toString() {
        return "FirmaDigitalResponse{id=" + id + "}";
    }
}
