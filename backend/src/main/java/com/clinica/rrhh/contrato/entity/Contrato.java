package com.clinica.rrhh.contrato.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.maestro.entity.rrhh.TipoContrato;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.type.EstadoContrato;
import com.clinica.rrhh.type.TipoJornada;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_contratos")
@AttributeOverride(name = "createdAt", column = @Column(name = "con_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "con_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "con_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Contrato extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "con_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "con_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "con_tipo_contrato_id", nullable = false)
    private TipoContrato tipoContrato;

    @Column(name = "con_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "con_fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "con_periodo_prueba_meses")
    private Integer periodoPruebaMeses;

    @Column(name = "con_remuneracion", nullable = false, precision = 10, scale = 2)
    private BigDecimal remuneracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "con_jornada", nullable = false, length = 20)
    private TipoJornada jornada = TipoJornada.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "con_estado", nullable = false, length = 20)
    private EstadoContrato estado = EstadoContrato.ACTIVO;

    @Column(name = "con_documento_id")
    private Long documentoId;

    @Column(name = "con_motivo_cese", length = 100)
    private String motivoCese;
}
