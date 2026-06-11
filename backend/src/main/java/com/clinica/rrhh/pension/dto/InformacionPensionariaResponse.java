package com.clinica.rrhh.pension.dto;

import com.clinica.rrhh.pension.entity.InformacionPensionaria;

import java.time.LocalDate;

public record InformacionPensionariaResponse(
    Long id,
    Long trabajadorId,
    Long afpId,
    String afpCodigo,
    String afpNombre,
    String cuspp,
    String comisionTipo,
    Boolean sctr,
    LocalDate fechaAfiliacion,
    String estado,
    String documentoReferencia
) {
    @Override
    public final String toString() {
        return "InformacionPensionariaResponse{id=" + id + ", estado=" + estado + "}";
    }

    public static InformacionPensionariaResponse fromEntity(InformacionPensionaria entity) {
        return new InformacionPensionariaResponse(
            entity.getId(),
            entity.getTrabajador().getId(),
            entity.getAfp().getId(),
            entity.getAfp().getCodigo(),
            entity.getAfp().getNombre(),
            entity.getCuspp(),
            entity.getComisionTipo(),
            entity.getSctr(),
            entity.getFechaAfiliacion(),
            entity.getEstado(),
            entity.getDocumentoReferencia()
        );
    }
}
