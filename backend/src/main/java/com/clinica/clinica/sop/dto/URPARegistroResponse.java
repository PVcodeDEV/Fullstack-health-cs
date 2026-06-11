package com.clinica.clinica.sop.dto;

import java.time.LocalDateTime;

public record URPARegistroResponse(
    Long id,
    Long reporteId,
    LocalDateTime fechaIngreso,
    LocalDateTime fechaSalida,
    String condicionIngreso,
    String condicionSalida,
    Integer escalaAldreteIngreso,
    Integer escalaAldreteSalida,
    String observaciones
) {
    @Override
    public final String toString() {
        return "URPARegistroResponse{id=" + id + "}";
    }
}
