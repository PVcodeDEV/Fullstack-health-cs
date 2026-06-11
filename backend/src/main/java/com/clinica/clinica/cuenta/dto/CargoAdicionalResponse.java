package com.clinica.clinica.cuenta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CargoAdicionalResponse(
    Long id,
    Long hospitalizacionId,
    String descripcion,
    BigDecimal monto,
    String tipoCargo,
    LocalDateTime fechaRegistro,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "CargoAdicionalResponse{id=" + id + ", descripcion=" + descripcion + "}";
    }
}
