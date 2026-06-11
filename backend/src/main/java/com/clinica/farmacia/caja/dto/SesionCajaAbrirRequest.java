package com.clinica.farmacia.caja.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SesionCajaAbrirRequest(

    @NotNull(message = "El almacén es obligatorio")
    Long almacenId,

    @NotNull(message = "El monto de apertura es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto de apertura no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El monto de apertura debe tener máximo 10 enteros y 2 decimales")
    BigDecimal montoApertura,

    @Size(max = 500, message = "Las observaciones no deben exceder 500 caracteres")
    String observaciones

) {
    public SesionCajaAbrirRequest {
        if (montoApertura == null) {
            montoApertura = BigDecimal.ZERO;
        }
        if (observaciones == null) {
            observaciones = "";
        }
    }
}
