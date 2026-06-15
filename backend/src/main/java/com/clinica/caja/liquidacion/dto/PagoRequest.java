package com.clinica.caja.liquidacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payment request for a Cuenta liquidación.
 */
public record PagoRequest(

    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres (PEN o USD)")
    String moneda,

    Long tipoCambioId,

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    BigDecimal descuento,

    Long usuarioApruebaId,

    @Valid
    @NotEmpty(message = "Debe incluir al menos un método de pago")
    List<PagoLegRequest> pagos
) {

    public PagoRequest {
        if (moneda == null || moneda.isBlank()) {
            moneda = "PEN";
        }
        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }
    }
}
