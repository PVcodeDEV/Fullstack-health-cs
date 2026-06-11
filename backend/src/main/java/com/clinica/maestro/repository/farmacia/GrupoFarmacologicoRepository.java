package com.clinica.maestro.repository.farmacia;

import com.clinica.maestro.entity.farmacia.GrupoFarmacologico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoFarmacologicoRepository extends JpaRepository<GrupoFarmacologico, Long> {

    List<GrupoFarmacologico> findAllByActivoTrueOrderByCodigo();

    Optional<GrupoFarmacologico> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
