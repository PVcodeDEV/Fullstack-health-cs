package com.clinica.clinica.hce.dto;

import java.time.LocalDateTime;

public record DocumentoClinicoResponse(
    Long id,
    Long hospitalizacionId,
    String tipoDocumento,
    String descripcion,
    Integer version,
    String usuarioNombre,
    LocalDateTime fechaCreacion,
    Boolean firmaPresente,
    String hashSha256
) {
    @Override
    public final String toString() {
        return "DocumentoClinicoResponse{id=" + id + ", tipo=" + tipoDocumento + "}";
    }
}
