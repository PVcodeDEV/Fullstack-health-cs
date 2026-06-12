package com.clinica.caja.tarifario.dto;

import com.clinica.caja.tarifario.entity.Paquete;
import com.clinica.caja.tarifario.entity.PaqueteDetalle;

import java.math.BigDecimal;
import java.util.List;

public record PaqueteResponse(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    BigDecimal precioTotal,
    List<PaqueteDetalleResponse> items,
    Boolean activo
) {
    public static PaqueteResponse fromEntity(Paquete entity, List<PaqueteDetalle> detalles) {
        List<PaqueteDetalleResponse> items = detalles.stream()
            .filter(PaqueteDetalle::getActivo)
            .map(d -> new PaqueteDetalleResponse(
                d.getId(),
                d.getTarifarioItem().getId(),
                d.getTarifarioItem().getCodigo(),
                d.getTarifarioItem().getNombre(),
                d.getCantidad()
            ))
            .toList();

        return new PaqueteResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getDescripcion(),
            entity.getPrecioTotal(),
            items,
            entity.getActivo()
        );
    }

    public record PaqueteDetalleResponse(
        Long id,
        Long tarifarioItemId,
        String tarifarioItemCodigo,
        String tarifarioItemNombre,
        Integer cantidad
    ) {}
}
