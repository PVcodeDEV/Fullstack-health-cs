package com.clinica.caja.tipocambio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TipoCambioRequest(

    @NotBlank(message = "La moneda origen es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda origen debe tener 3 caracteres")
    String monedaOrigen,

    @NotBlank(message = "La moneda destino es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda destino debe tener 3 caracteres")
    String monedaDestino,

    @NotNull(message = "El tipo de cambio es obligatorio")
    @DecimalMin(value = "0.0001", message = "El tipo de cambio debe ser mayor a cero")
    @Digits(integer = 6, fraction = 4, message = "El tipo de cambio debe tener máximo 6 enteros y 4 decimales")
    BigDecimal tipoCambio,

    @NotNull(message = "La fecha es obligatoria")
    LocalDate fecha,

    @NotNull(message = "El usuario es obligatorio")
    Long usuarioId

) {}
