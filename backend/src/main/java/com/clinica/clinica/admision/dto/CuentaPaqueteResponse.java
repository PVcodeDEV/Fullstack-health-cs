package com.clinica.clinica.admision.dto;

import java.math.BigDecimal;

public record CuentaPaqueteResponse(
    Long id,
    Long cuentaId,
    Long paqueteId,
    String paqueteNombre,
    BigDecimal monto,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "CuentaPaqueteResponse{id=" + id + ", paquete=" + paqueteNombre + "}";
    }
}
