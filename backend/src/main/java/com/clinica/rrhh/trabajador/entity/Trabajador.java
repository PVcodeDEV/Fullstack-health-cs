package com.clinica.rrhh.trabajador.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.maestro.entity.rrhh.TipoColegiatura;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.periodo.entity.PeriodoLaboral;
import com.clinica.rrhh.type.RegimenLaboral;
import com.clinica.rrhh.type.TipoTrabajador;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tb_trabajadores")
@AttributeOverride(name = "createdAt", column = @Column(name = "tra_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tra_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tra_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trabajador extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tra_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tra_persona_id", nullable = false, unique = true)
    private Persona persona;

    @Column(name = "tra_codigo_trabajador", nullable = false, unique = true, length = 20)
    private String codigoTrabajador;

    @Column(name = "tra_fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "tra_cargo", length = 200)
    private String cargo;

    @Column(name = "tra_area_funcional_id")
    private Long areaFuncionalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tra_tipo", length = 30)
    private TipoTrabajador tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tra_regimen_laboral", length = 20)
    private RegimenLaboral regimenLaboral;

    @Column(name = "tra_motivo_cese", length = 100)
    private String motivoCese;

    @Column(name = "tra_banco", length = 60)
    private String banco;

    @Column(name = "tra_cuenta_sueldo", length = 20)
    private String cuentaSueldo;

    @Column(name = "tra_cci", length = 23)
    private String cci;

    @Column(name = "tra_contacto_nombre", length = 150)
    private String contactoNombre;

    @Column(name = "tra_contacto_telefono", length = 15)
    private String contactoTelefono;

    @Column(name = "tra_cantidad_hijos", nullable = false)
    private Integer cantidadHijos = 0;

    @Column(name = "tra_colegiatura_numero", length = 20)
    private String nroColegiatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tra_colegiatura_tipo_id")
    private TipoColegiatura tipoColegiatura;

    @Column(name = "tra_discapacidad", nullable = false)
    private Boolean discapacidad = false;

    @Column(name = "tra_sindicalizado", nullable = false)
    private Boolean sindicalizado = false;

    @OneToMany(mappedBy = "trabajador")
    private List<PeriodoLaboral> periodosLaborales;
}
