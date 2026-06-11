package com.clinica.rrhh.planilla.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_planilla_detalles")
@AttributeOverride(name = "createdAt", column = @Column(name = "pde_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "pde_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "pde_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PlanillaDetalle extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pde_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pde_planilla_id", nullable = false)
    private Planilla planilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pde_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pde_contrato_id")
    private Contrato contrato;

    @Column(name = "pde_sueldo_base", precision = 10, scale = 2)
    private BigDecimal sueldoBase;

    @Column(name = "pde_asignacion_familiar", nullable = false, precision = 10, scale = 2)
    private BigDecimal asignacionFamiliar = BigDecimal.ZERO;

    @Column(name = "pde_dias_laborados", nullable = false)
    private Integer diasLaborados = 30;

    @Column(name = "pde_total_ingresos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Column(name = "pde_total_descuentos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDescuentos = BigDecimal.ZERO;

    @Column(name = "pde_total_aportes", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAportes = BigDecimal.ZERO;

    @Column(name = "pde_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal neto = BigDecimal.ZERO;

    @Column(name = "pde_conceptos_json", columnDefinition = "TEXT")
    private String conceptosJson;
}
