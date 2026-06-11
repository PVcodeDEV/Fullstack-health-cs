package com.clinica.seguridad.repository;

import com.clinica.seguridad.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
