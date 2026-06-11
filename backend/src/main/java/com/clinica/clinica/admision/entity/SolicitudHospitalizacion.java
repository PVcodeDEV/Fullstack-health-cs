package com.clinica.clinica.admision.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_solicitudes_hospitalizacion")
@AttributeOverride(name = "createdAt", column = @Column(name = "sol_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "sol_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "sol_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudHospitalizacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sol_id")
    private Long id;

    @Column(name = "sol_cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "sol_tipo_habitacion_id", nullable = false)
    private Long tipoHabitacionId;

    @Column(name = "sol_cama_id")
    private Long camaId;

    @Column(name = "sol_estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "sol_fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "sol_fecha_ingreso")
    private LocalDateTime fechaIngreso;
}
