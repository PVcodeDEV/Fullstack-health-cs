package com.clinica.persona.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.maestro.entity.identidad.EstadoCivil;
import com.clinica.maestro.entity.identidad.TipoDocumentoIdentidad;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "tb_personas")
@AttributeOverride(name = "createdAt", column = @Column(name = "pers_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "pers_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "pers_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Persona extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pers_persona_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pers_tipo_documento_id", nullable = false)
    private TipoDocumentoIdentidad tipoDocumentoIdentidad;

    @Column(name = "pers_numero_documento", nullable = false, unique = true, length = 20)
    @ToString.Exclude
    private String numeroDocumento;

    @Column(name = "pers_nombres", length = 200)
    @ToString.Exclude
    private String nombres;

    @Column(name = "pers_apellido_paterno", length = 100)
    @ToString.Exclude
    private String apellidoPaterno;

    @Column(name = "pers_apellido_materno", length = 100)
    @ToString.Exclude
    private String apellidoMaterno;

    @Column(name = "pers_fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "pers_sexo", length = 1)
    private String sexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pers_estado_civil_id")
    private EstadoCivil estadoCivil;

    @Column(name = "pers_direccion", length = 255)
    @ToString.Exclude
    private String direccion;

    @Column(name = "pers_ubigeo_distrito", length = 6)
    private String ubigeoDistrito;

    @Column(name = "pers_telefono", length = 20)
    @ToString.Exclude
    private String telefono;

    @Column(name = "pers_email", length = 100)
    @ToString.Exclude
    private String email;

    @Column(name = "pers_fecha_ultima_consulta")
    private LocalDate fechaUltimaConsulta;
}
