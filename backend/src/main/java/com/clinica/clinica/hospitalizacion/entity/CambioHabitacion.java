package com.clinica.clinica.hospitalizacion.entity;

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
@Table(name = "tb_cambios_habitacion")
@AttributeOverride(name = "createdAt", column = @Column(name = "cam_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "cam_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "cam_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambioHabitacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cam_id")
    private Long id;

    @Column(name = "cam_hospitalizacion_id", nullable = false)
    private Long hospitalizacionId;

    @Column(name = "cam_cama_origen_id", nullable = false)
    private Long camaOrigenId;

    @Column(name = "cam_cama_destino_id", nullable = false)
    private Long camaDestinoId;

    @Column(name = "cam_usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "cam_fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "cam_motivo", length = 255)
    private String motivo;
}
