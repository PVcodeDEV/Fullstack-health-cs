package com.clinica.farmacia.reposicion.entity;

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

/**
 * Detalle de una lista de reposición.
 * <p>
 * Cada fila representa un producto que necesita reposición,
 * con el stock actual, stock mínimo, y la cantidad sugerida.
 * </p>
 */
@Entity
@Table(name = "tb_reposicion_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReposicionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rdet_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rdet_reposicion_id", referencedColumnName = "rep_id", nullable = false)
    private Reposicion reposicion;

    @Column(name = "rdet_producto_id", nullable = false)
    private Long productoId;

    @Column(name = "rdet_stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "rdet_stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "rdet_stock_critico")
    private Integer stockCritico;

    @Column(name = "rdet_cantidad_sugerida", nullable = false)
    private Integer cantidadSugerida;

    @Column(name = "rdet_proveedor_sugerido", length = 200)
    private String proveedorSugerido;
}
