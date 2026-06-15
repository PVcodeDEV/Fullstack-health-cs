package com.clinica.caja.liquidacion.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment transaction record for a Cuenta.
 * Created when a cashier processes payment for a Cuenta that
 * is PENDIENTE_COBRO. Links to SesionCaja and optionally TipoCambio.
 */
@Entity
@Table(name = "caja_liquidacion")
@AttributeOverride(name = "createdAt", column = @Column(name = "liq_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "liq_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "liq_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Liquidacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liq_id")
    private Long id;

    @Column(name = "liq_cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "liq_sesion_id", nullable = false)
    private Long sesionId;

    @Column(name = "liq_fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "liq_moneda", nullable = false, length = 3)
    private String moneda = "PEN";

    @Column(name = "liq_monto_total", nullable = false, precision = 10, scale = 2)
    @ToString.Exclude
    private BigDecimal montoTotal;

    @Column(name = "liq_monto_usd", precision = 10, scale = 2)
    @ToString.Exclude
    private BigDecimal montoUSD;

    @Column(name = "liq_monto_pen", precision = 10, scale = 2)
    @ToString.Exclude
    private BigDecimal montoPEN;

    @Column(name = "liq_tipo_cambio_id")
    private Long tipoCambioId;

    @Column(name = "liq_descuento_total", nullable = false, precision = 10, scale = 2)
    @ToString.Exclude
    private BigDecimal descuentoTotal = BigDecimal.ZERO;

    @Column(name = "liq_descuento_porcentaje", precision = 5, scale = 2)
    @ToString.Exclude
    private BigDecimal descuentoPorcentaje;

    @Column(name = "liq_usuario_aprueba_id")
    private Long usuarioApruebaId;

    @Column(name = "liq_fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "liq_usuario_cobra_id", nullable = false)
    private Long usuarioCobraId;

    @Column(name = "liq_estado", nullable = false, length = 20)
    private String estado = "PAGADO";

    public enum Estado {
        PAGADO
    }
}
