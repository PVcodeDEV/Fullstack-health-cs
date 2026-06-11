package com.clinica.rrhh.cts.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_depositos_cts")
@AttributeOverride(name = "createdAt", column = @Column(name = "dct_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "dct_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "dct_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class DepositoCts extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dct_id")
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dct_periodo_planilla_id", nullable = false)
    private PeriodoPlanilla periodoPlanilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dct_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dct_contrato_id")
    private Contrato contrato;

    @Column(name = "dct_semestre", nullable = false, length = 20)
    @ToString.Include
    private String semestre;

    @Column(name = "dct_dias_computables", nullable = false)
    @ToString.Include
    private Integer diasComputables;

    @Column(name = "dct_remuneracion_computable", precision = 10, scale = 2)
    private BigDecimal remuneracionComputable;

    @Column(name = "dct_promedio_gratificacion", precision = 10, scale = 2)
    private BigDecimal promedioGratificacion;

    @Column(name = "dct_promedio_bonificacion", precision = 10, scale = 2)
    private BigDecimal promedioBonificacion;

    @Column(name = "dct_monto_cts", precision = 10, scale = 2)
    private BigDecimal montoCts;

    @Column(name = "dct_estado", nullable = false, length = 20)
    @ToString.Include
    private String estado = "CALCULADO";
}
