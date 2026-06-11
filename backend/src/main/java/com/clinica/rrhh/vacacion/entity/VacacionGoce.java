package com.clinica.rrhh.vacacion.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_vacaciones_goces")
@AttributeOverride(name = "createdAt", column = @Column(name = "vgo_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "vgo_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "vgo_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class VacacionGoce extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vgo_id")
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vgo_record_id", nullable = false)
    private VacacionRecord record;

    @Column(name = "vgo_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "vgo_fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "vgo_dias", nullable = false)
    @ToString.Include
    private Integer dias;

    @Column(name = "vgo_remuneracion", precision = 10, scale = 2)
    private BigDecimal remuneracion;

    @Column(name = "vgo_estado", nullable = false, length = 20)
    @ToString.Include
    private String estado = "PROGRAMADO";
}
