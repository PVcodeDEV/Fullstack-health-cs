package com.clinica.entidad.entity;

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
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "entidad_sunat_consulta_log")
@AttributeOverride(name = "createdAt", column = @Column(name = "sun_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "sun_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "sun_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SunatConsultaLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sun_id")
    private Long id;

    @Column(name = "sun_ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "sun_fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "sun_ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "sun_usuario_id")
    private Long usuarioId;

    @Column(name = "sun_respuesta_raw", length = 2000)
    @ToString.Exclude
    private String respuestaRaw;

    @Column(name = "sun_exito", nullable = false)
    private Boolean exito;
}
