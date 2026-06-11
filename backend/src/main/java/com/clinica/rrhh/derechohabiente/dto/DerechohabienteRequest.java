package com.clinica.rrhh.derechohabiente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DerechohabienteRequest(
    @NotNull Long trabajadorId,
    @NotNull Long personaId,
    @NotBlank String relacion,
    @NotNull LocalDate fechaInicio,
    LocalDate fechaFin
) {}
