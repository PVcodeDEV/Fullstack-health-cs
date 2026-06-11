package com.clinica.rrhh.planillaplame.repository;

import com.clinica.rrhh.planillaplame.entity.ArchivoPlanilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArchivoPlanillaRepository extends JpaRepository<ArchivoPlanilla, Long> {

    List<ArchivoPlanilla> findByPeriodoPlanillaId(Long periodoPlanillaId);

    Optional<ArchivoPlanilla> findByPeriodoPlanillaIdAndTipo(Long periodoPlanillaId, String tipo);

    boolean existsByPeriodoPlanillaIdAndTipo(Long periodoPlanillaId, String tipo);
}
