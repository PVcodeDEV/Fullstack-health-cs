package com.clinica.seguridad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioRolId implements Serializable {

    @Column(name = "usro_usuario_id")
    private Long usuarioId;

    @Column(name = "usro_rol_id")
    private Long rolId;
}
