package com.clinica.clinica.sop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReporteQuirurgicoRequest(
    Long hospitalizacionId,
    @NotNull Long cirujanoId,
    Long anestesiologoId,
    @NotBlank String diagnosticoPreoperatorio,
    @NotBlank String procedimientoRealizado,
    String hallazgos,
    String complicaciones,
    @NotNull LocalDate fechaCirugia,
    @NotNull LocalTime horaInicio,
    LocalTime horaFin,
    String estado
) {}
