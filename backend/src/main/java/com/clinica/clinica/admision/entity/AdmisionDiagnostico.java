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

@Entity
@Table(name = "tb_admision_diagnosticos")
@AttributeOverride(name = "createdAt", column = @Column(name = "diag_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "diag_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "diag_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdmisionDiagnostico extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diag_id")
    private Long id;

    @Column(name = "diag_cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "diag_codigo_cie11", nullable = false, length = 8)
    private String codigoCIE11;

    @Column(name = "diag_tipo", nullable = false, length = 15)
    private String tipo = "PRINCIPAL";
}
