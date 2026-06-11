package com.clinica.farmacia.caja.entity;

import com.clinica.farmacia.caja.type.EstadoSesion;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Sesión de caja de Farmacia.
 * <p>
 * Representa una apertura/cierre de caja para un cajero (usuario).
 * Cada venta se asocia a una sesión abierta. Al cerrar la sesión
 * se calcula la diferencia entre el monto esperado y el real.
 * </p>
 */
@Entity
@Table(name = "tb_sesiones_caja")
@AttributeOverride(name = "createdAt", column = @Column(name = "scaj_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "scaj_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "scaj_activo"))
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SesionCaja extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scaj_id")
    @Setter
    private Long id;

    @Column(name = "scaj_usuario_id", nullable = false)
    @Setter
    private Long usuarioId;

    @Column(name = "scaj_almacen_id")
    @Setter
    private Long almacenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scaj_estado", nullable = false, length = 16)
    private EstadoSesion estado = EstadoSesion.ABIERTA;

    @Column(name = "scaj_monto_apertura", nullable = false, precision = 12, scale = 2)
    @Setter
    private BigDecimal montoApertura = BigDecimal.ZERO;

    @Column(name = "scaj_monto_cierre_esperado", precision = 12, scale = 2)
    private BigDecimal montoCierreEsperado;

    @Column(name = "scaj_monto_cierre_real", precision = 12, scale = 2)
    private BigDecimal montoCierreReal;

    @Column(name = "scaj_diferencia_cierre", precision = 12, scale = 2)
    private BigDecimal diferenciaCierre;

    @Column(name = "scaj_total_ventas", nullable = false, precision = 12, scale = 2)
    @Setter
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(name = "scaj_fecha_apertura", nullable = false)
    @Setter
    private LocalDateTime fechaApertura;

    @Column(name = "scaj_fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "scaj_observaciones_apertura", length = 500)
    private String observacionesApertura;

    @Column(name = "scaj_observaciones_cierre", length = 500)
    private String observacionesCierre;

    // === Setters for estado (both typed and backward-compatible String version) ===

    /**
     * Typed setter for estado (EstadoSesion enum).
     * Used by new SesionCajaService code.
     */
    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }

    /**
     * Backward-compatible setter for existing VentaServiceTest
     * that pass String values like "ABIERTA".
     */
    public void setEstado(String estadoStr) {
        this.estado = EstadoSesion.valueOf(estadoStr);
    }

    // === Helper methods ===

    /**
     * Open a cash session with the given parameters.
     */
    public void abrir(Long usuarioId, Long almacenId, BigDecimal montoApertura, String observaciones) {
        this.usuarioId = usuarioId;
        this.almacenId = almacenId;
        this.montoApertura = montoApertura;
        this.observacionesApertura = observaciones;
        this.estado = EstadoSesion.ABIERTA;
        this.fechaApertura = LocalDateTime.now();
        this.totalVentas = BigDecimal.ZERO;
    }

    /**
     * Close the cash session, computing the difference.
     *
     * @param montoCierreReal the actually counted cash
     * @param observaciones   closing notes
     */
    public void cerrar(BigDecimal montoCierreReal, String observaciones) {
        this.montoCierreReal = montoCierreReal;
        this.montoCierreEsperado = montoApertura.add(totalVentas);
        this.diferenciaCierre = montoCierreReal.subtract(montoCierreEsperado)
            .setScale(2, RoundingMode.HALF_UP);
        this.fechaCierre = LocalDateTime.now();
        this.estado = EstadoSesion.CERRADA;
        this.observacionesCierre = observaciones;
    }

    /**
     * Add a venta amount to the session totals.
     * Called by {@code VentaService.completar()} after a successful sale.
     *
     * @param montoVenta the total amount of the completed sale
     */
    public void agregarVenta(BigDecimal montoVenta) {
        this.totalVentas = totalVentas.add(montoVenta);
    }
}
