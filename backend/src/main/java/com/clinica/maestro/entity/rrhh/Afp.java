package com.clinica.maestro.entity.rrhh;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_afps")
@AttributeOverride(name = "createdAt", column = @Column(name = "afp_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "afp_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "afp_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Afp extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "afp_id")
    private Long id;

    @Column(name = "afp_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "afp_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "afp_descripcion", length = 255)
    private String descripcion;
}
