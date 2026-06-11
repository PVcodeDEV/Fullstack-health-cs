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
@Table(name = "tb_cuentas_paquetes")
@AttributeOverride(name = "createdAt", column = @Column(name = "cup_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "cup_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "cup_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPaquete extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cup_id")
    private Long id;

    @Column(name = "cup_cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "cup_paquete_quirurgico_id", nullable = false)
    private Long paqueteQuirurgicoId;

    @Column(name = "cup_fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
