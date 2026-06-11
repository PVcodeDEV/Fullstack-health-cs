package com.clinica.farmacia.reposicion.dto;

/**
 * Projection for products with low stock.
 * Returned by the aggregation query in {@code ProductoRepository}.
 * <p>
 * Note: stockActual is Long because JPQL SUM(integer_column) returns Long
 * due to Hibernate type resolution.
 * </p>
 */
public record ProductoStockBajoProjection(
    Long productoId,
    String productoCodigo,
    String productoDescripcion,
    Integer stockMinimo,
    Integer stockCritico,
    Long stockActual
) {
}
