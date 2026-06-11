package com.clinica.maestro.entity.ubigeo;
import com.clinica.maestro.entity.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_ubigeo_provincias")
@AttributeOverride(name = "createdAt", column = @Column(name = "ubprov_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "ubprov_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "ubprov_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbigeoProvincia extends BaseEntity {

    @Id
    @Column(name = "ubprov_codigo", length = 4)
    private String codigo;

    @Column(name = "ubprov_nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubprov_departamento", referencedColumnName = "ubdep_codigo", nullable = false)
    private UbigeoDepartamento departamento;
}
