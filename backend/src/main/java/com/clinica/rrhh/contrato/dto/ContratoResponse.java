package com.clinica.rrhh.contrato.dto;

import com.clinica.rrhh.contrato.entity.Contrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoResponse(
    Long id,
    Long trabajadorId,
    Long tipoContratoId,
    String tipoContratoNombre,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Integer periodoPruebaMeses,
    BigDecimal remuneracion,
    String jornada,
    String estado,
    String motivoCese,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "ContratoResponse{id=" + id + ", estado=" + estado + "}";
    }

    public static ContratoResponse fromEntity(Contrato entity) {
        return new ContratoResponse(
            entity.getId(),
            entity.getTrabajador().getId(),
            entity.getTipoContrato().getId(),
            entity.getTipoContrato().getNombre(),
            entity.getFechaInicio(),
            entity.getFechaFin(),
            entity.getPeriodoPruebaMeses(),
            entity.getRemuneracion(),
            entity.getJornada().name(),
            entity.getEstado().name(),
            entity.getMotivoCese(),
            entity.getActivo()
        );
    }
}
