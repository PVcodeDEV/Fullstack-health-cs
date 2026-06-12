package com.clinica.caja.tarifario.dto;

import com.clinica.caja.tarifario.entity.TarifarioItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrecioResponse(
    String codigo,
    BigDecimal precioBase,
    BigDecimal precioFinal,
    LocalDate fechaDesde
) {
    public static PrecioResponse fromEntity(TarifarioItem entity) {
        return new PrecioResponse(
            entity.getCodigo(),
            entity.getPrecioBase(),
            entity.getPrecioFinal(),
            entity.getFechaDesde()
        );
    }
}
