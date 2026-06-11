package com.clinica.rrhh.periodo.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_periodos_laborales")
@AttributeOverride(name = "createdAt", column = @Column(name = "pla_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "pla_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "pla_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PeriodoLaboral extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pla_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pla_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @Column(name = "pla_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "pla_fecha_cese")
    private LocalDate fechaCese;

    @Column(name = "pla_motivo_cese", length = 100)
    private String motivoCese;

    @Column(name = "pla_es_reingreso", nullable = false)
    private Boolean esReingreso = false;
}
