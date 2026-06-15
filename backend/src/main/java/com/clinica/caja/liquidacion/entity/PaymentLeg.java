package com.clinica.caja.liquidacion.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Individual payment leg within a Liquidacion.
 * Supports split payments across multiple methods (Efectivo, POS, YAPE/PLIN, Transferencia).
 */
@Entity
@Table(name = "caja_payment_leg")
@AttributeOverride(name = "createdAt", column = @Column(name = "pag_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "pag_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "pag_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class PaymentLeg extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pag_liquidacion_id", nullable = false)
    private Liquidacion liquidacion;

    @Column(name = "pag_metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "pag_monto", nullable = false, precision = 10, scale = 2)
    @ToString.Exclude
    private BigDecimal monto;

    @Column(name = "pag_referencia", length = 100)
    private String referencia;
}
