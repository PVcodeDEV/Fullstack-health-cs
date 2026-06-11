package com.clinica.rrhh.derechohabiente.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.type.TipoRelacionDerechohabiente;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_derechohabientes")
@AttributeOverride(name = "createdAt", column = @Column(name = "der_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "der_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "der_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Derechohabiente extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "der_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "der_trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "der_persona_id", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "der_relacion", nullable = false, length = 20)
    private TipoRelacionDerechohabiente relacion;

    @Column(name = "der_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "der_fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "der_estado", nullable = false, length = 20)
    private String estado = "ACTIVO";
}
