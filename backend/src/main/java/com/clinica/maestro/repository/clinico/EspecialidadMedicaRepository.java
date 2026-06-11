package com.clinica.maestro.repository.clinico;

import com.clinica.maestro.entity.clinico.EspecialidadMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialidadMedicaRepository extends JpaRepository<EspecialidadMedica, Long> {

    List<EspecialidadMedica> findAllByOrderByNombreAsc();

    Optional<EspecialidadMedica> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
