package com.clinica.seguridad.repository;

import com.clinica.seguridad.entity.RolPermiso;
import com.clinica.seguridad.entity.RolPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermisoId> {

    List<RolPermiso> findByRolId(Long rolId);

    List<RolPermiso> findByPermisoId(Long permisoId);
}
