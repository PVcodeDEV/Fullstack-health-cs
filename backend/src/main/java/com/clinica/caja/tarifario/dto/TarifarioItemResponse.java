package com.clinica.caja.tarifario.dto;

import com.clinica.caja.tarifario.entity.TarifarioItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifarioItemResponse(
    Long id,
    Long tarifarioId,
    String codigo,
    String nombre,
    String descripcion,
    BigDecimal precioBase,
    BigDecimal precioFinal,
    Integer unidadMedidaId,
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    Boolean activo
) {
    public static TarifarioItemResponse fromEntity(TarifarioItem entity) {
        return new TarifarioItemResponse(
            entity.getId(),
            entity.getTarifario().getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getDescripcion(),
            entity.getPrecioBase(),
            entity.getPrecioFinal(),
            entity.getUnidadMedidaId(),
            entity.getFechaDesde(),
            entity.getFechaHasta(),
            entity.getActivo()
        );
    }
}
