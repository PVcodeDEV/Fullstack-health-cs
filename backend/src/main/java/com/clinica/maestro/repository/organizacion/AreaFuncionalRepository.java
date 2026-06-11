package com.clinica.maestro.repository.organizacion;

import com.clinica.maestro.entity.organizacion.AreaFuncional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaFuncionalRepository extends JpaRepository<AreaFuncional, Integer> {

    List<AreaFuncional> findAllByOrderByNombreAsc();

    Optional<AreaFuncional> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<AreaFuncional> findByEsAreaFisica(Boolean esAreaFisica);
}
