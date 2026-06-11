package com.clinica.rrhh.periodo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CeseRequest(
    @NotNull(message = "La fecha de cese es obligatoria")
    LocalDate fechaCese,

    @NotBlank(message = "El motivo de cese es obligatorio")
    String motivo
) {}
