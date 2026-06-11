package com.clinica.maestro.entity.ubigeo;
import com.clinica.maestro.entity.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_ubigeo_departamentos")
@AttributeOverride(name = "createdAt", column = @Column(name = "ubdep_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "ubdep_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "ubdep_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbigeoDepartamento extends BaseEntity {

    @Id
    @Column(name = "ubdep_codigo", length = 2)
    private String codigo;

    @Column(name = "ubdep_nombre", nullable = false, length = 100)
    private String nombre;
}
