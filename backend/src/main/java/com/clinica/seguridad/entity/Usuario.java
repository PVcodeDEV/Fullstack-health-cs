package com.clinica.seguridad.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_usuarios")
@AttributeOverride(name = "createdAt", column = @Column(name = "usu_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "usu_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "usu_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_persona_id", nullable = false, unique = true)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_trabajador_id", unique = true)
    private Trabajador trabajador;

    @Column(name = "usu_username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "usu_password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "usu_last_login")
    private LocalDateTime lastLogin;
}
