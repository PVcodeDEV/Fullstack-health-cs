package com.clinica.rrhh.trabajador.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReingresoRequest(
    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio
) {}
