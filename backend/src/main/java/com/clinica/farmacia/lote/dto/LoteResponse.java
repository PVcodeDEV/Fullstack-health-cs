package com.clinica.farmacia.lote.dto;

import com.clinica.farmacia.lote.entity.Lote;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteResponse(
    Long id,
    Long productoId,
    String codigoLote,
    LocalDate fechaVencimiento,
    Integer stockInicial,
    Integer stockActual,
    BigDecimal precioCosto,
    Long almacenId,
    Boolean activo
) {
    public static LoteResponse fromEntity(Lote entity) {
        return new LoteResponse(
            entity.getId(),
            entity.getProducto().getId(),
            entity.getCodigoLote(),
            entity.getFechaVencimiento(),
            entity.getStockInicial(),
            entity.getStockActual(),
            entity.getPrecioCosto(),
            entity.getAlmacen().getId(),
            entity.getActivo()
        );
    }
}
