package com.clinica.seguridad.dto;

import com.clinica.seguridad.entity.ConfiguracionApi;

public record ConfiguracionApiResponse(
    Long id,
    String modulo,
    String clave,
    String valor,
    String tipo,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "ConfiguracionApiResponse{id=" + id
            + ", modulo=" + modulo
            + ", clave=" + clave
            + ", tipo=" + tipo
            + ", activo=" + activo
            + "}";
    }

    public static ConfiguracionApiResponse fromEntity(ConfiguracionApi entity) {
        return new ConfiguracionApiResponse(
            entity.getId(),
            entity.getModulo(),
            entity.getClave(),
            entity.getValor(),
            entity.getTipo(),
            entity.getActivo()
        );
    }
}
