package com.clinica.maestro.entity.rrhh;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_tipos_colegiatura")
@AttributeOverride(name = "createdAt", column = @Column(name = "tcl_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tcl_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tcl_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoColegiatura extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tcl_id")
    private Long id;

    @Column(name = "tcl_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "tcl_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tcl_descripcion", length = 255)
    private String descripcion;
}
