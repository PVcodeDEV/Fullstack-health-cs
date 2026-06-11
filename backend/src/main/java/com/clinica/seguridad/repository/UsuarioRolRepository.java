package com.clinica.seguridad.repository;

import com.clinica.seguridad.entity.UsuarioRol;
import com.clinica.seguridad.entity.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    List<UsuarioRol> findByRolId(Long rolId);

    List<UsuarioRol> findByUsuarioId(Long usuarioId);
}
