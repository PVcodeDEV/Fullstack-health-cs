package com.clinica.maestro.entity.rrhh;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_tipos_contrato")
@AttributeOverride(name = "createdAt", column = @Column(name = "tco_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tco_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tco_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoContrato extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tco_id")
    private Long id;

    @Column(name = "tco_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "tco_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tco_descripcion", length = 255)
    private String descripcion;
}
