package com.clinica.clinica.cama.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HabitacionRequest(
    @NotNull Long tipoHabitacionId,
    @NotBlank String nombre,
    @NotBlank String ubicacion,
    Integer capacidad,
    String observaciones
) {}
