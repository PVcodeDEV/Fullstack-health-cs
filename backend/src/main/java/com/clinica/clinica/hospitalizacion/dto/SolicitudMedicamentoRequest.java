package com.clinica.clinica.hospitalizacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SolicitudMedicamentoRequest(
    @NotNull Long medicamentoId,
    @NotBlank String dosis,
    @NotBlank String frecuencia,
    Long viaAdministracionId,
    @NotNull LocalDate fechaInicio,
    LocalDate fechaFin
) {}
