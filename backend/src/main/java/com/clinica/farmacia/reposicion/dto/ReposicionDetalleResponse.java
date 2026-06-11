package com.clinica.farmacia.reposicion.dto;

import com.clinica.farmacia.reposicion.entity.ReposicionDetalle;

public record ReposicionDetalleResponse(
    Long id,
    Long productoId,
    String productoCodigo,
    String productoDescripcion,
    Integer stockActual,
    Integer stockMinimo,
    Integer stockCritico,
    Integer cantidadSugerida,
    String proveedorSugerido
) {
    public static ReposicionDetalleResponse fromEntity(ReposicionDetalle entity, String codigo, String descripcion) {
        return new ReposicionDetalleResponse(
            entity.getId(),
            entity.getProductoId(),
            codigo,
            descripcion,
            entity.getStockActual(),
            entity.getStockMinimo(),
            entity.getStockCritico(),
            entity.getCantidadSugerida(),
            entity.getProveedorSugerido()
        );
    }
}
