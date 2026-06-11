package com.clinica.rrhh.planillaplame.dto;

import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import java.time.LocalDateTime;

public record ArchivoPlanillaResponse(
    Long id,
    Long periodoPlanillaId,
    String tipo,
    String hash,
    String generadoPor,
    Boolean activo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ArchivoPlanillaResponse fromEntity(ArchivoPlanilla entity) {
        return new ArchivoPlanillaResponse(
            entity.getId(),
            entity.getPeriodoPlanilla().getId(),
            entity.getTipo(),
            entity.getHash(),
            entity.getGeneradoPor(),
            entity.getActivo(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    @Override
    public final String toString() {
        return "ArchivoPlanillaResponse{id=" + id + ", tipo=" + tipo + "}";
    }
}
