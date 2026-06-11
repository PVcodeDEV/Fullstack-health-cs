package com.clinica.maestro.entity.financiero;
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
@Table(name = "tb_tipos_moneda")
@AttributeOverride(name = "createdAt", column = @Column(name = "tmon_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tmon_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tmon_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoMoneda extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tmon_id")
    private Integer id;

    @Column(name = "tmon_codigo_sunat", nullable = false, unique = true, length = 3)
    private String codigoSunat;

    @Column(name = "tmon_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tmon_simbolo", nullable = false, length = 5)
    private String simbolo;
}
