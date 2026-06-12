package com.clinica.caja.tipocambio.dto;

import com.clinica.caja.tipocambio.entity.TipoCambio;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TipoCambioResponse(
    Long id,
    String monedaOrigen,
    String monedaDestino,
    BigDecimal tipoCambio,
    LocalDate fecha,
    Long usuarioId
) {
    public static TipoCambioResponse fromEntity(TipoCambio entity) {
        return new TipoCambioResponse(
            entity.getId(),
            entity.getMonedaOrigen(),
            entity.getMonedaDestino(),
            entity.getTipoCambio(),
            entity.getFecha(),
            entity.getUsuarioId()
        );
    }
}
