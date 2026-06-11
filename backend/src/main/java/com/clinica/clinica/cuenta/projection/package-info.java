/**
 * Extraction boundary for module-to-module integration.
 * <p>
 * {@link CuentaParaCaja} is the agreed extraction point for the future Caja module.
 * When Caja is implemented, it will read CuentaParaCaja projections to determine
 * pending payments and confirmed charges.
 * </p>
 * <p>
 * Current boundary (MVP): account lives entirely within clinica module.
 * Future: Caja module reads CuentaParaCaja, writes payment confirmation back.
 * </p>
 */
@ExtractionPoint("Caja module will extract CuentaParaCaja for payment processing")
package com.clinica.clinica.cuenta.projection;

import com.clinica.maestro.annotation.ExtractionPoint;
