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
@Table(name = "tb_ubigeo_distritos")
@AttributeOverride(name = "createdAt", column = @Column(name = "ubdist_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "ubdist_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "ubdist_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbigeoDistrito extends BaseEntity {

    @Id
    @Column(name = "ubdist_codigo", length = 6)
    private String codigo;

    @Column(name = "ubdist_nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubdist_provincia", referencedColumnName = "ubprov_codigo", nullable = false)
    private UbigeoProvincia provincia;
}
