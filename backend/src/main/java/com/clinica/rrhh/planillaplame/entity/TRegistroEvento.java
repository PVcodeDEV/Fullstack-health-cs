package com.clinica.rrhh.planillaplame.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_tregistro_eventos")
@AttributeOverride(name = "createdAt", column = @Column(name = "tre_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tre_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tre_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class TRegistroEvento extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tre_id")
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tre_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tre_contrato_id")
    private Contrato contrato;

    @Column(name = "tre_tipo_evento", nullable = false, length = 20)
    @ToString.Include
    private String tipoEvento;

    @Column(name = "tre_fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tre_periodo_planilla_id")
    private PeriodoPlanilla periodoPlanilla;

    @Column(name = "tre_detalle_json", columnDefinition = "TEXT")
    private String detalleJson;

    @Column(name = "tre_estado", nullable = false, length = 20)
    @ToString.Include
    private String estado = "PENDIENTE";
}
