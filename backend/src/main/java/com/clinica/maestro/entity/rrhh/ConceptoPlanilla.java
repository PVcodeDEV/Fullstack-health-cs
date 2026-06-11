package com.clinica.maestro.entity.rrhh;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_conceptos_planilla")
@AttributeOverride(name = "createdAt", column = @Column(name = "cpl_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "cpl_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "cpl_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConceptoPlanilla extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpl_id")
    private Long id;

    @Column(name = "cpl_codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "cpl_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cpl_tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "cpl_formula", length = 100)
    private String formula;

    @Column(name = "cpl_orden")
    private Integer orden = 0;
}
