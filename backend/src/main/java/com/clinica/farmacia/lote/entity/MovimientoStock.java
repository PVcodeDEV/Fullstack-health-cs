package com.clinica.farmacia.lote.entity;

import com.clinica.farmacia.almacen.entity.Almacen;
import com.clinica.farmacia.lote.type.TipoMovimiento;
import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_movimientos_stock")
@AttributeOverride(name = "createdAt", column = @Column(name = "movs_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "movs_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "movs_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movs_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movs_lote_id", referencedColumnName = "lote_id", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    @Column(name = "movs_tipo", nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(name = "movs_cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "movs_motivo", length = 255)
    private String motivo;

    @Column(name = "movs_venta_id")
    private Long ventaId;

    @Column(name = "movs_usuario_id")
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movs_almacen_origen_id", referencedColumnName = "alm_id")
    private Almacen almacenOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movs_almacen_destino_id", referencedColumnName = "alm_id")
    private Almacen almacenDestino;
}
