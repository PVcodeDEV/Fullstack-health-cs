package com.clinica.maestro.entity.clinico;

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
@Table(name = "tb_cie11_diagnosticos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CIE11Diagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cie_id")
    private Long id;

    @Column(name = "cie_codigo", nullable = false, unique = true, length = 8)
    private String codigo;

    @Column(name = "cie_descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "cie_categoria", nullable = false, length = 1)
    private String categoria;

    @Column(name = "cie_sexo_aplicable", nullable = false, length = 5)
    private String sexoAplicable = "AMBOS";

    @Column(name = "cie_edad_minina")
    private Integer edadMinima;

    @Column(name = "cie_edad_maxima")
    private Integer edadMaxima;

    @Column(name = "cie_version", nullable = false, length = 10)
    private String version = "CIE-11";

    @Column(name = "cie_frecuencia_uso")
    private Integer frecuenciaUso = 0;
}
