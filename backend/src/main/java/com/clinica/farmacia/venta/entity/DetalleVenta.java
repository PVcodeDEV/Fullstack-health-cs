package com.clinica.farmacia.venta.entity;

import com.clinica.farmacia.lote.entity.Lote;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_detalle_ventas")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dvt_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dvt_venta_id", referencedColumnName = "vent_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dvt_lote_id", referencedColumnName = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "dvt_cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "dvt_precio_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "dvt_precio_original", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioOriginal;

    @Column(name = "dvt_descuento_aplicado", nullable = false, precision = 12, scale = 4)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;

    @Column(name = "dvt_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "dvt_created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
