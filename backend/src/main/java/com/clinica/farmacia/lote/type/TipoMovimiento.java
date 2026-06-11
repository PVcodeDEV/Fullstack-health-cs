package com.clinica.farmacia.lote.type;

/**
 * Types of stock movements in the pharmacy module.
 * DEVOLUCION added for Venta anulation (stock restoration).
 */
public enum TipoMovimiento {
    ENTRADA,
    SALIDA,
    AJUSTE,
    TRANSFERENCIA,
    DEVOLUCION
}
