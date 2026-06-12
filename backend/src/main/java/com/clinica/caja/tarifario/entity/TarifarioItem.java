package com.clinica.caja.tarifario.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "caja_tarifario_item",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tait_codigo", "tait_fecha_desde"}))
@AttributeOverride(name = "createdAt", column = @Column(name = "tait_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tait_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tait_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class TarifarioItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tait_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tait_tarifario_id", nullable = false)
    private Tarifario tarifario;

    @Column(name = "tait_codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "tait_nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "tait_descripcion", length = 500)
    private String descripcion;

    @Column(name = "tait_precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "tait_precio_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(name = "tait_unidad_medida_id")
    private Integer unidadMedidaId;

    @Column(name = "tait_fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "tait_fecha_hasta")
    private LocalDate fechaHasta;

    /**
     * Returns true if this item is currently active (no end date set).
     */
    public boolean isCurrentlyActive() {
        return fechaHasta == null;
    }
}
