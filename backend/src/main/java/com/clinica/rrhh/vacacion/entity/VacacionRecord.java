package com.clinica.rrhh.vacacion.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_vacaciones_record")
@AttributeOverride(name = "createdAt", column = @Column(name = "vcr_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "vcr_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "vcr_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class VacacionRecord extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vcr_id")
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vcr_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vcr_contrato_id")
    private Contrato contrato;

    @Column(name = "vcr_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "vcr_fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "vcr_dias_derecho", nullable = false)
    @ToString.Include
    private Integer diasDerecho = 15;

    @Column(name = "vcr_dias_reduccion", nullable = false)
    @ToString.Include
    private Integer diasReduccion = 0;

    @Column(name = "vcr_dias_pendientes", nullable = false, precision = 5, scale = 2)
    @ToString.Include
    private BigDecimal diasPendientes;

    @Column(name = "vcr_estado", nullable = false, length = 20)
    @ToString.Include
    private String estado = "ACTIVO";

    @Column(name = "vcr_fecha_expiracion", nullable = false)
    private LocalDate fechaExpiracion;
}
