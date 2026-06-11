package com.clinica.farmacia.reposicion.dto;

import com.clinica.farmacia.reposicion.entity.Reposicion;
import com.clinica.farmacia.reposicion.type.EstadoReposicion;

import java.time.LocalDateTime;
import java.util.List;

public record ReposicionResponse(
    Long id,
    LocalDateTime generadaEn,
    Long usuarioId,
    Long almacenId,
    String observaciones,
    EstadoReposicion estado,
    LocalDateTime procesadaEn,
    List<ReposicionDetalleResponse> detalles
) {
    public static ReposicionResponse fromEntity(Reposicion entity, List<ReposicionDetalleResponse> detalles) {
        return new ReposicionResponse(
            entity.getId(),
            entity.getGeneradaEn(),
            entity.getUsuarioId(),
            entity.getAlmacenId(),
            entity.getObservaciones(),
            entity.getEstado(),
            entity.getProcesadaEn(),
            detalles
        );
    }
}
