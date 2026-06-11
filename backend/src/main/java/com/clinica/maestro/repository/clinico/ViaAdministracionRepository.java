package com.clinica.maestro.repository.clinico;

import com.clinica.maestro.entity.clinico.ViaAdministracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViaAdministracionRepository extends JpaRepository<ViaAdministracion, Long> {

    List<ViaAdministracion> findAllByOrderByNombreAsc();

    Optional<ViaAdministracion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
