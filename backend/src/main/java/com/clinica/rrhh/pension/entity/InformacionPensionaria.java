package com.clinica.rrhh.pension.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.maestro.entity.rrhh.Afp;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_informacion_pensionaria")
@AttributeOverride(name = "createdAt", column = @Column(name = "inf_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "inf_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "inf_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InformacionPensionaria extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inf_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inf_trabajador_id", nullable = false, unique = true)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inf_afp_id", nullable = false)
    private Afp afp;

    @Column(name = "inf_cuspp", length = 16)
    @ToString.Exclude
    private String cuspp;

    @Column(name = "inf_comision_tipo", length = 20)
    private String comisionTipo;

    @Column(name = "inf_sctr", nullable = false)
    private Boolean sctr = false;

    @Column(name = "inf_fecha_afiliacion", nullable = false)
    private LocalDate fechaAfiliacion;

    @Column(name = "inf_estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "inf_documento_referencia", length = 50)
    private String documentoReferencia;
}
