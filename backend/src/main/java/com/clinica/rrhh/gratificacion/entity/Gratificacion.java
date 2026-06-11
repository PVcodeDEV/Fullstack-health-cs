package com.clinica.rrhh.gratificacion.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_gratificaciones")
@AttributeOverride(name = "createdAt", column = @Column(name = "gra_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "gra_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "gra_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Gratificacion extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gra_id")
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gra_periodo_planilla_id", nullable = false)
    private PeriodoPlanilla periodoPlanilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gra_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gra_contrato_id")
    private Contrato contrato;

    @Column(name = "gra_semestre", nullable = false, length = 20)
    @ToString.Include
    private String semestre;

    @Column(name = "gra_meses_computables", nullable = false)
    @ToString.Include
    private Integer mesesComputables;

    @Column(name = "gra_remuneracion_computable", precision = 10, scale = 2)
    private BigDecimal remuneracionComputable;

    @Column(name = "gra_gratificacion", precision = 10, scale = 2)
    private BigDecimal gratificacion;

    @Column(name = "gra_bonificacion_extraordinaria", precision = 10, scale = 2)
    private BigDecimal bonificacionExtraordinaria;

    @Column(name = "gra_total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "gra_estado", nullable = false, length = 20)
    @ToString.Include
    private String estado = "CALCULADO";
}
