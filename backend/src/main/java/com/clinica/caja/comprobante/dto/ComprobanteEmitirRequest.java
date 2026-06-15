package com.clinica.caja.comprobante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request to emit an electronic comprobante (Boleta or Factura).
 * Client references are mutually exclusive: provide personaId (Boleta)
 * or empresaId (Factura), not both.
 */
public record ComprobanteEmitirRequest(

    @NotBlank(message = "El tipo de comprobante es obligatorio (01=Factura, 03=Boleta)")
    @Size(min = 2, max = 2, message = "El tipo de comprobante debe tener 2 dígitos")
    String tipoComprobante,

    String serie,

    Long personaId,

    Long empresaId,

    BigDecimal montoTotal

) {}
