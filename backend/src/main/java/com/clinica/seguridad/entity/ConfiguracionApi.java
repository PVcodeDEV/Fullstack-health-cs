package com.clinica.seguridad.entity;

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
@Table(name = "tb_configuracion_api")
@AttributeOverride(name = "createdAt", column = @Column(name = "conf_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "conf_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "conf_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionApi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conf_id")
    private Long id;

    @Column(name = "conf_modulo", nullable = false, length = 50)
    private String modulo;

    @Column(name = "conf_clave", nullable = false, length = 100)
    private String clave;

    @Column(name = "conf_valor", columnDefinition = "TEXT")
    private String valor;

    @Column(name = "conf_tipo", nullable = false, length = 20)
    private String tipo = "string";
}
