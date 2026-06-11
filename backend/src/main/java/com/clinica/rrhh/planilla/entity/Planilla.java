package com.clinica.rrhh.planilla.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_planillas")
@AttributeOverride(name = "createdAt", column = @Column(name = "pla_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "pla_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "pla_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Planilla extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pla_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pla_periodo_planilla_id", nullable = false)
    private PeriodoPlanilla periodoPlanilla;

    @Column(name = "pla_fecha_liquidacion")
    private LocalDate fechaLiquidacion;

    @Column(name = "pla_total_ingresos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Column(name = "pla_total_descuentos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDescuentos = BigDecimal.ZERO;

    @Column(name = "pla_total_aportes", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAportes = BigDecimal.ZERO;

    @Column(name = "pla_total_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalNeto = BigDecimal.ZERO;

    @Column(name = "pla_cantidad_trabajadores", nullable = false)
    private Integer cantidadTrabajadores = 0;

    @Column(name = "pla_estado", nullable = false, length = 20)
    private String estado = "BORRADOR";
}
