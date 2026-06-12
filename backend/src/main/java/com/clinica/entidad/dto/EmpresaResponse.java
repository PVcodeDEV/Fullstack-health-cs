package com.clinica.entidad.dto;

import com.clinica.entidad.entity.Empresa;

public record EmpresaResponse(
    Long id,
    String ruc,
    String tipoRuc,
    String razonSocial,
    String direccionFiscal,
    String ubigeo,
    String telefono,
    String email,
    String estado,
    String rol,
    Long personaId,
    Boolean activo
) {
    public static EmpresaResponse fromEntity(Empresa entity) {
        return new EmpresaResponse(
            entity.getId(),
            entity.getRuc(),
            entity.getTipoRuc().name(),
            entity.getRazonSocial(),
            entity.getDireccionFiscal(),
            entity.getUbigeo(),
            entity.getTelefono(),
            entity.getEmail(),
            entity.getEstado().name(),
            entity.getRol().name(),
            entity.getPersonaId(),
            entity.getActivo()
        );
    }
}
