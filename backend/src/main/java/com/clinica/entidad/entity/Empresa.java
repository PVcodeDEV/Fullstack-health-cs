package com.clinica.entidad.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "entidad_empresa")
@AttributeOverride(name = "createdAt", column = @Column(name = "emp_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "emp_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "emp_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Empresa extends BaseEntity {

    public enum TipoRuc {
        RUC_10,
        RUC_20
    }

    public enum Estado {
        ACTIVO,
        INACTIVO
    }

    public enum Rol {
        CLIENTE,
        PROVEEDOR,
        AMBOS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Long id;

    @Column(name = "emp_ruc", nullable = false, unique = true, length = 11)
    @ToString.Exclude
    private String ruc;

    @Enumerated(EnumType.STRING)
    @Column(name = "emp_tipo_ruc", nullable = false, length = 10)
    private TipoRuc tipoRuc;

    @Column(name = "emp_razon_social", length = 255)
    private String razonSocial;

    @Column(name = "emp_direccion_fiscal", length = 255)
    private String direccionFiscal;

    @Column(name = "emp_ubigeo", length = 6)
    private String ubigeo;

    @Column(name = "emp_telefono", length = 20)
    @ToString.Exclude
    private String telefono;

    @Column(name = "emp_email", length = 100)
    @ToString.Exclude
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "emp_estado", nullable = false, length = 10)
    private Estado estado = Estado.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "emp_rol", nullable = false, length = 15)
    private Rol rol = Rol.CLIENTE;

    @Column(name = "emp_persona_id")
    private Long personaId;
}
