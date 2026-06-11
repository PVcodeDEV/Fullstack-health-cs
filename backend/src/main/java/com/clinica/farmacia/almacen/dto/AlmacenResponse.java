package com.clinica.farmacia.almacen.dto;

import com.clinica.farmacia.almacen.entity.Almacen;

public record AlmacenResponse(
    Long id,
    String codigo,
    String nombre,
    String ubicacion,
    Boolean defaultWarehouse,
    Boolean activo
) {
    public static AlmacenResponse fromEntity(Almacen entity) {
        return new AlmacenResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getUbicacion(),
            entity.getDefaultWarehouse(),
            entity.getActivo()
        );
    }
}
