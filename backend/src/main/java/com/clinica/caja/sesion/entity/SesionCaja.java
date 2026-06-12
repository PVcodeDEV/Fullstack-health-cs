package com.clinica.caja.sesion.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Clinical cash register session.
 * Entity name qualified as {@code ClinicaSesionCaja} to avoid clash with
 * {@code com.clinica.farmacia.caja.entity.SesionCaja}.
 */
@Entity(name = "ClinicaSesionCaja")
@Table(name = "caja_sesion_caja")
@AttributeOverride(name = "createdAt", column = @Column(name = "ses_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "ses_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "ses_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SesionCaja extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ses_id")
    private Long id;

    @Column(name = "ses_codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "ses_usuario_apertura_id", nullable = false)
    private Long usuarioAperturaId;

    @Column(name = "ses_fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "ses_monto_apertura", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoApertura;

    @Enumerated(EnumType.STRING)
    @Column(name = "ses_estado", nullable = false, length = 20)
    private Estado estado;

    @Column(name = "ses_usuario_cierre_id")
    private Long usuarioCierreId;

    @Column(name = "ses_fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "ses_monto_cierre", precision = 10, scale = 2)
    private BigDecimal montoCierre;

    @Column(name = "ses_total_ventas", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(name = "ses_diferencia", precision = 10, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "ses_observaciones", length = 500)
    private String observaciones;

    public enum Estado {
        ABIERTA, CERRADA
    }

    /**
     * Close the session with the given closing amount and server timestamp.
     * Computes diferencia = montoCierre - montoApertura - totalVentas.
     *
     * @param montoCierre  actual cash in drawer at close
     * @param usuarioId    cashier closing the session
     * @param now          server timestamp
     */
    public void cerrar(BigDecimal montoCierre, Long usuarioId, LocalDateTime now) {
        this.estado = Estado.CERRADA;
        this.montoCierre = montoCierre;
        this.usuarioCierreId = usuarioId;
        this.fechaCierre = now;
        this.diferencia = montoCierre
            .subtract(this.montoApertura)
            .subtract(this.totalVentas != null ? this.totalVentas : BigDecimal.ZERO);
    }
}
