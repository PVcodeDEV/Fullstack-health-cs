package com.clinica.clinica.cuenta.entity;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cargos_adicionales")
@AttributeOverride(name = "createdAt", column = @Column(name = "cta_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "cta_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "cta_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoAdicional extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cta_id")
    private Long id;

    @Column(name = "cta_cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "cta_tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "cta_monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "cta_descripcion", length = 255)
    private String descripcion;

    @Column(name = "cta_fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "cta_usuario_id", nullable = false)
    private Long usuarioId;
}
