package com.clinica.farmacia.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    String codigo,

    @NotNull(message = "El tipo es obligatorio (MEDICAMENTO o INSUMO)")
    String tipo,

    // === Common ===

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.0001", message = "El precio de costo debe ser mayor a cero")
    @Digits(integer = 8, fraction = 4, message = "El precio de costo debe tener máximo 8 enteros y 4 decimales")
    BigDecimal precioCosto,

    @DecimalMin(value = "0", message = "La utilidad no puede ser negativa")
    @Digits(integer = 3, fraction = 2, message = "La utilidad debe tener máximo 3 enteros y 2 decimales")
    BigDecimal utilidadMedico,

    @DecimalMin(value = "0", message = "La utilidad no puede ser negativa")
    @Digits(integer = 3, fraction = 2, message = "La utilidad debe tener máximo 3 enteros y 2 decimales")
    BigDecimal utilidadPublico,

    Integer stockMinimo,

    Integer stockCritico,

    Long categoriaInsumoId,

    Long unidadMedidaId,

    // === MEDICAMENTO fields ===

    @Size(max = 255, message = "El nombre genérico no debe exceder 255 caracteres")
    String generico,

    String descripcion,

    Boolean origen,

    Long tipoMedicamentoId,

    Long formaFarmaceuticaId,

    Long formaPresentacionId,

    Long grupoFarmacologicoId,

    // === INSUMO fields ===

    Long marcaId

) {

    /**
     * Validates that required fields are present based on type.
     * MEDICAMENTO requires: tipoMedicamentoId, formaFarmaceuticaId, formaPresentacionId, grupoFarmacologicoId
     * INSUMO requires: marcaId
     */
    public void validateTypeFields() {
        if ("MEDICAMENTO".equalsIgnoreCase(tipo)) {
            if (tipoMedicamentoId == null) {
                throw new IllegalArgumentException("El tipo de medicamento es obligatorio para MEDICAMENTO");
            }
            if (formaFarmaceuticaId == null) {
                throw new IllegalArgumentException("La forma farmacéutica es obligatoria para MEDICAMENTO");
            }
            if (formaPresentacionId == null) {
                throw new IllegalArgumentException("La forma de presentación es obligatoria para MEDICAMENTO");
            }
            if (grupoFarmacologicoId == null) {
                throw new IllegalArgumentException("El grupo farmacológico es obligatorio para MEDICAMENTO");
            }
        } else if ("INSUMO".equalsIgnoreCase(tipo)) {
            if (marcaId == null) {
                throw new IllegalArgumentException("La marca es obligatoria para INSUMO");
            }
        } else {
            throw new IllegalArgumentException("Tipo inválido: debe ser MEDICAMENTO o INSUMO");
        }
    }
}
