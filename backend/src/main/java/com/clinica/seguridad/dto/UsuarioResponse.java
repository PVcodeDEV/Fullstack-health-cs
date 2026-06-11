package com.clinica.seguridad.dto;

import com.clinica.seguridad.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(
    Long id,
    Long personaId,
    String personaNombre,
    String username,
    LocalDateTime lastLogin,
    Boolean activo,
    List<String> roles
) {
    @Override
    public final String toString() {
        return "UsuarioResponse{id=" + id
            + ", personaId=" + personaId
            + ", username=" + username
            + ", activo=" + activo
            + "}";
    }

    public static UsuarioResponse fromEntity(Usuario entity, List<String> roles) {
        String personaNombre = entity.getPersona() != null
            ? entity.getPersona().getNombres() + " " + entity.getPersona().getApellidoPaterno()
            : null;
        return new UsuarioResponse(
            entity.getId(),
            entity.getPersona() != null ? entity.getPersona().getId() : null,
            personaNombre,
            entity.getUsername(),
            entity.getLastLogin(),
            entity.getActivo(),
            roles
        );
    }
}
