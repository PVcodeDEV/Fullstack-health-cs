package com.clinica.persona.dto;

import com.clinica.persona.entity.Persona;

import java.time.LocalDate;

public record PersonaResponse(
    Long id,
    Long tipoDocumentoId,
    String tipoDocumentoNombre,
    String numeroDocumento,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    LocalDate fechaNacimiento,
    String sexo,
    Long estadoCivilId,
    String estadoCivilNombre,
    String direccion,
    String ubigeoDistrito,
    String telefono,
    String email,
    LocalDate fechaUltimaConsulta,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "PersonaResponse{id=" + id
            + ", tipoDocumentoId=" + tipoDocumentoId
            + ", tipoDocumentoNombre=" + tipoDocumentoNombre
            + ", fechaNacimiento=" + fechaNacimiento
            + ", sexo=" + sexo
            + ", estadoCivilId=" + estadoCivilId
            + ", fechaUltimaConsulta=" + fechaUltimaConsulta
            + ", activo=" + activo
            + "}";
    }
    public static PersonaResponse fromEntity(Persona entity) {
        return new PersonaResponse(
            entity.getId(),
            entity.getTipoDocumentoIdentidad() != null ? entity.getTipoDocumentoIdentidad().getId() : null,
            entity.getTipoDocumentoIdentidad() != null ? entity.getTipoDocumentoIdentidad().getNombre() : null,
            entity.getNumeroDocumento(),
            entity.getNombres(),
            entity.getApellidoPaterno(),
            entity.getApellidoMaterno(),
            entity.getFechaNacimiento(),
            entity.getSexo(),
            entity.getEstadoCivil() != null ? entity.getEstadoCivil().getId() : null,
            entity.getEstadoCivil() != null ? entity.getEstadoCivil().getNombre() : null,
            entity.getDireccion(),
            entity.getUbigeoDistrito(),
            entity.getTelefono(),
            entity.getEmail(),
            entity.getFechaUltimaConsulta(),
            entity.getActivo()
        );
    }
}
