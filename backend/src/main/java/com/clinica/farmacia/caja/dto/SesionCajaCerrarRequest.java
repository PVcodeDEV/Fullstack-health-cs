package com.clinica.farmacia.caja.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SesionCajaCerrarRequest(

    @NotNull(message = "El monto real de cierre es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto de cierre no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El monto de cierre debe tener máximo 10 enteros y 2 decimales")
    BigDecimal montoCierreReal,

    @Size(max = 500, message = "Las observaciones no deben exceder 500 caracteres")
    String observaciones

) {
    public SesionCajaCerrarRequest {
        if (observaciones == null) {
            observaciones = "";
        }
    }
}
