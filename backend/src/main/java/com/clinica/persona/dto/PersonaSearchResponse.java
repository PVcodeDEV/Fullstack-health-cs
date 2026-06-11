package com.clinica.persona.dto;

import com.clinica.persona.entity.Persona;

public record PersonaSearchResponse(
    Long id,
    String tipoDocumentoNombre,
    String numeroDocumento,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno
) {
    public static PersonaSearchResponse fromEntity(Persona entity) {
        return new PersonaSearchResponse(
            entity.getId(),
            entity.getTipoDocumentoIdentidad() != null ? entity.getTipoDocumentoIdentidad().getNombre() : null,
            entity.getNumeroDocumento(),
            entity.getNombres(),
            entity.getApellidoPaterno(),
            entity.getApellidoMaterno()
        );
    }
}
