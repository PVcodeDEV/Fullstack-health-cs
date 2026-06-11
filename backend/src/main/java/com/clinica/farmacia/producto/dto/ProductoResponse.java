package com.clinica.farmacia.producto.dto;

import com.clinica.farmacia.producto.entity.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
    Long id,
    String codigo,
    String tipo,
    BigDecimal precioCosto,
    BigDecimal utilidadMedico,
    BigDecimal utilidadPublico,
    BigDecimal precioVentaMedico,
    BigDecimal precioVentaPublico,
    Integer stockMinimo,
    Integer stockCritico,
    Long categoriaInsumoId,
    String categoriaInsumoNombre,
    Long unidadMedidaId,
    String unidadMedidaNombre,
    String generico,
    String descripcion,
    Boolean origen,
    Long tipoMedicamentoId,
    String tipoMedicamentoNombre,
    Long formaFarmaceuticaId,
    String formaFarmaceuticaNombre,
    Long formaPresentacionId,
    String formaPresentacionNombre,
    Long grupoFarmacologicoId,
    String grupoFarmacologicoNombre,
    Long marcaId,
    String marcaNombre,
    Boolean activo
) {
    public static ProductoResponse fromEntity(Producto entity) {
        return new ProductoResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getTipo().name(),
            entity.getPrecioCosto(),
            entity.getUtilidadMedico(),
            entity.getUtilidadPublico(),
            entity.getPrecioVentaMedico(),
            entity.getPrecioVentaPublico(),
            entity.getStockMinimo(),
            entity.getStockCritico(),
            entity.getCategoriaInsumo() != null ? entity.getCategoriaInsumo().getId().longValue() : null,
            entity.getCategoriaInsumo() != null ? entity.getCategoriaInsumo().getNombre() : null,
            entity.getUnidadMedida() != null ? entity.getUnidadMedida().getId().longValue() : null,
            entity.getUnidadMedida() != null ? entity.getUnidadMedida().getNombre() : null,
            entity.getGenerico(),
            entity.getDescripcion(),
            entity.getOrigen(),
            entity.getTipoMedicamento() != null ? entity.getTipoMedicamento().getId() : null,
            entity.getTipoMedicamento() != null ? entity.getTipoMedicamento().getNombre() : null,
            entity.getFormaFarmaceutica() != null ? entity.getFormaFarmaceutica().getId() : null,
            entity.getFormaFarmaceutica() != null ? entity.getFormaFarmaceutica().getNombre() : null,
            entity.getFormaPresentacion() != null ? entity.getFormaPresentacion().getId() : null,
            entity.getFormaPresentacion() != null ? entity.getFormaPresentacion().getNombre() : null,
            entity.getGrupoFarmacologico() != null ? entity.getGrupoFarmacologico().getId() : null,
            entity.getGrupoFarmacologico() != null ? entity.getGrupoFarmacologico().getNombre() : null,
            entity.getMarca() != null ? entity.getMarca().getId() : null,
            entity.getMarca() != null ? entity.getMarca().getNombre() : null,
            entity.getActivo()
        );
    }
}
