package com.clinica.maestro.dto.clinico;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TipoHabitacionRequest(
    @NotBlank @Size(max = 10) String codigo,
    @NotBlank @Size(max = 100) String nombre,
    @NotNull @DecimalMin("0.00") BigDecimal tarifaBase,
    @NotNull @Min(1) Integer capacidad
) {}
