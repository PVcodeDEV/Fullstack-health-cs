package com.clinica.farmacia.venta.entity;

import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.venta.type.EstadoVenta;
import com.clinica.farmacia.venta.type.TipoLista;
import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_ventas")
@AttributeOverride(name = "createdAt", column = @Column(name = "vent_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "vent_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "vent_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vent_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vent_sesion_caja_id", referencedColumnName = "scaj_id", nullable = false)
    private SesionCaja sesionCaja;

    @Column(name = "vent_correlativo", nullable = false)
    private Integer correlativo;

    @Column(name = "vent_cliente_persona_id")
    private Long clientePersonaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vent_tipo_lista", nullable = false, length = 16)
    private TipoLista tipoLista = TipoLista.PUBLICO;

    @Column(name = "vent_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "vent_descuento_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoTotal = BigDecimal.ZERO;

    @Column(name = "vent_impuesto", nullable = false, precision = 12, scale = 2)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Column(name = "vent_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "vent_estado", nullable = false, length = 16)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @Column(name = "vent_vendedor_usuario_id", nullable = false)
    private Long vendedorUsuarioId;

    @Column(name = "vent_con_impresion", nullable = false)
    private Boolean conImpresion = false;

    @Column(name = "vent_observaciones", length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles = new ArrayList<>();

    // === Helper methods ===

    public void addDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void removeDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
    }

    /**
     * Recomputes subtotal, descuentoTotal, impuesto, and total from detalles.
     * Uses the lifecycle discount rules already applied to each detalle.
     * IGV = 18% of subtotal (included, so impuesto = total * 18/118).
     */
    public void calcularTotales() {
        BigDecimal sumaSubtotal = BigDecimal.ZERO;
        BigDecimal sumaDescuentos = BigDecimal.ZERO;

        for (DetalleVenta d : detalles) {
            sumaSubtotal = sumaSubtotal.add(d.getSubtotal());
            sumaDescuentos = sumaDescuentos.add(d.getDescuentoAplicado());
        }

        this.subtotal = sumaSubtotal.setScale(2, RoundingMode.HALF_UP);
        this.descuentoTotal = sumaDescuentos.setScale(2, RoundingMode.HALF_UP);

        // IGV = total * 18/118 (IGV incluido en precio de venta)
        // Primero calculamos el total como subtotal - descuentoTotal
        BigDecimal base = subtotal.subtract(descuentoTotal);
        // IGV = base * 18 / 118
        this.impuesto = base.multiply(new BigDecimal("18"))
            .divide(new BigDecimal("118"), 2, RoundingMode.HALF_UP);
        // Total = base
        this.total = base.setScale(2, RoundingMode.HALF_UP);
    }
}
