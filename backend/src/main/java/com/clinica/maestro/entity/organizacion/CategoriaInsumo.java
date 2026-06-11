package com.clinica.maestro.entity.organizacion;
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

@Entity
@Table(name = "tb_categorias_insumo")
@AttributeOverride(name = "createdAt", column = @Column(name = "categ_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "categ_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "categ_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaInsumo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categ_id")
    private Integer id;

    @Column(name = "categ_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "categ_nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categ_categoria_padre_id", referencedColumnName = "categ_id")
    private CategoriaInsumo categoriaPadre;
}
