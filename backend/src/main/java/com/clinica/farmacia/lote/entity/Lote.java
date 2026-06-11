package com.clinica.farmacia.lote.entity;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.producto.entity.Producto;
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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_lotes")
@AttributeOverride(name = "createdAt", column = @Column(name = "lote_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "lote_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "lote_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lote_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_producto_id", referencedColumnName = "prod_id", nullable = false)
    private Producto producto;

    @Column(name = "lote_codigo_lote", nullable = false, length = 100)
    private String codigoLote;

    @Column(name = "lote_fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "lote_stock_inicial", nullable = false)
    private Integer stockInicial;

    @Version
    @Column(name = "lote_version", nullable = false)
    private Integer version;

    @Column(name = "lote_stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "lote_precio_costo", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioCosto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_almacen_id", referencedColumnName = "alm_id", nullable = false)
    private Almacen almacen;
}
