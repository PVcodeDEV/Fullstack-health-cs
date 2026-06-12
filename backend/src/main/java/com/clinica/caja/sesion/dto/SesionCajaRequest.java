package com.clinica.caja.sesion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SesionCajaRequest(

    @NotNull(message = "El monto de apertura es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto de apertura debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El monto debe tener máximo 8 enteros y 2 decimales")
    BigDecimal montoApertura

) {}
