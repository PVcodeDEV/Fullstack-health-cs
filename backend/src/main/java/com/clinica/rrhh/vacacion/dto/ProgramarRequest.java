package com.clinica.rrhh.vacacion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProgramarRequest(
    @NotNull Long trabajadorId,
    @NotNull LocalDate fechaInicio,
    @NotNull @Min(7) Integer dias
) {}
