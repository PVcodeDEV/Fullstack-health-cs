package com.clinica.caja.tarifario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceChangeRequest(

    @NotBlank(message = "El código del item es obligatorio")
    String codigo,

    @NotNull(message = "El nuevo precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El nuevo precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 enteros y 2 decimales")
    BigDecimal nuevoPrecio,

    @NotNull(message = "La fecha desde es obligatoria")
    LocalDate fechaDesde

) {}
