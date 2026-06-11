package com.clinica.maestro.entity.organizacion;
import com.clinica.maestro.entity.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_areas_funcionales")
@AttributeOverride(name = "createdAt", column = @Column(name = "areaf_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "areaf_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "areaf_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AreaFuncional extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "areaf_id")
    private Integer id;

    @Column(name = "areaf_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "areaf_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "areaf_es_area_fisica", nullable = false)
    private Boolean esAreaFisica = false;
}
