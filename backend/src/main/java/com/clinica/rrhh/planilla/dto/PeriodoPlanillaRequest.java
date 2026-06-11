package com.clinica.rrhh.planilla.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PeriodoPlanillaRequest(
    @NotNull @Min(2020) @Max(2100) Integer anio,
    @NotNull @Min(1) @Max(12) Integer mes,
    @NotNull LocalDate fechaInicio,
    @NotNull LocalDate fechaFin
) {}
