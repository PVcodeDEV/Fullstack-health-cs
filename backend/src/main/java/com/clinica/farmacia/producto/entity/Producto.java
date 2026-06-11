package com.clinica.farmacia.producto.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.maestro.entity.farmacia.FormaPresentacion;
import com.clinica.maestro.entity.farmacia.GrupoFarmacologico;
import com.clinica.maestro.entity.farmacia.Marca;
import com.clinica.maestro.entity.farmacia.TipoMedicamento;
import com.clinica.maestro.entity.clinico.FormaFarmaceutica;
import com.clinica.maestro.entity.financiero.UnidadMedida;
import com.clinica.maestro.entity.organizacion.CategoriaInsumo;
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

import java.math.BigDecimal;

@Entity
@Table(name = "tb_productos")
@AttributeOverride(name = "createdAt", column = @Column(name = "prod_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "prod_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "prod_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto extends BaseEntity {

    public enum TipoProducto {
        MEDICAMENTO,
        INSUMO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_id")
    private Long id;

    @Column(name = "prod_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "prod_tipo", nullable = false, length = 20)
    private TipoProducto tipo;

    // === Common fields ===

    @Column(name = "prod_precio_costo", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioCosto;

    @Column(name = "prod_utilidad_medico", precision = 5, scale = 2)
    private BigDecimal utilidadMedico;

    @Column(name = "prod_utilidad_publico", precision = 5, scale = 2)
    private BigDecimal utilidadPublico;

    @Column(name = "prod_precio_venta_medico", precision = 10, scale = 2)
    private BigDecimal precioVentaMedico;

    @Column(name = "prod_precio_venta_publico", precision = 10, scale = 2)
    private BigDecimal precioVentaPublico;

    @Column(name = "prod_stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(name = "prod_stock_critico", nullable = false)
    private Integer stockCritico = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_categoria_insumo_id", referencedColumnName = "categ_id")
    private CategoriaInsumo categoriaInsumo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_unidad_medida_id", referencedColumnName = "umed_id")
    private UnidadMedida unidadMedida;

    // === MEDICAMENTO fields ===

    @Column(name = "prod_generico", length = 255)
    private String generico;

    @Column(name = "prod_descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "prod_origen")
    private Boolean origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_tipo_medicamento_id", referencedColumnName = "tmed_id")
    private TipoMedicamento tipoMedicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_forma_farmaceutica_id", referencedColumnName = "ffar_id")
    private FormaFarmaceutica formaFarmaceutica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_forma_presentacion_id", referencedColumnName = "fpre_id")
    private FormaPresentacion formaPresentacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_grupo_farmacologico_id", referencedColumnName = "gfar_id")
    private GrupoFarmacologico grupoFarmacologico;

    // === INSUMO fields ===

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_marca_id", referencedColumnName = "marc_id")
    private Marca marca;
}
