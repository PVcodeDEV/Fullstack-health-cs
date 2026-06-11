package com.clinica.maestro.repository.clinico;

import com.clinica.maestro.entity.clinico.FormaFarmaceutica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormaFarmaceuticaRepository extends JpaRepository<FormaFarmaceutica, Long> {

    List<FormaFarmaceutica> findAllByOrderByNombreAsc();

    Optional<FormaFarmaceutica> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
