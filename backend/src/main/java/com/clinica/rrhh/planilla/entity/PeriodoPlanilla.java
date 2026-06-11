package com.clinica.rrhh.planilla.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_periodos_planilla")
@AttributeOverride(name = "createdAt", column = @Column(name = "ppl_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "ppl_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "ppl_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PeriodoPlanilla extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ppl_id")
    private Long id;

    @Column(name = "ppl_anio", nullable = false)
    private Integer anio;

    @Column(name = "ppl_mes", nullable = false)
    private Integer mes;

    @Column(name = "ppl_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "ppl_fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "ppl_estado", nullable = false, length = 20)
    private String estado = "ABIERTO";
}
