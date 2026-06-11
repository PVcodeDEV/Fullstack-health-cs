package com.clinica.maestro.dto.organizacion;

import com.clinica.maestro.entity.organizacion.CategoriaInsumo;

public record CategoriaInsumoResponse(
    Integer id,
    String codigo,
    String nombre,
    Integer categoriaPadreId,
    String categoriaPadreNombre,
    Boolean activo
) {
    public static CategoriaInsumoResponse fromEntity(CategoriaInsumo entity) {
        return new CategoriaInsumoResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getCategoriaPadre() != null ? entity.getCategoriaPadre().getId() : null,
            entity.getCategoriaPadre() != null ? entity.getCategoriaPadre().getNombre() : null,
            entity.getActivo()
        );
    }
}
