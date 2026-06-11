package com.clinica.rrhh.contrato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoRequest(

    @NotNull(message = "El trabajador es obligatorio")
    Long trabajadorId,

    @NotNull(message = "El tipo de contrato es obligatorio")
    Long tipoContratoId,

    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio,

    LocalDate fechaFin,

    @Positive(message = "El periodo de prueba debe ser positivo")
    Integer periodoPruebaMeses,

    @NotNull(message = "La remuneración es obligatoria")
    @Positive(message = "La remuneración debe ser positiva")
    BigDecimal remuneracion,

    String jornada        // default REGULAR if null

) {}
