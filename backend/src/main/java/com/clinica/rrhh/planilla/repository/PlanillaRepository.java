package com.clinica.rrhh.planilla.repository;

import com.clinica.rrhh.planilla.entity.Planilla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanillaRepository extends JpaRepository<Planilla, Long> {
    List<Planilla> findAllByOrderByPeriodoPlanillaAnioDescPeriodoPlanillaMesDesc();
    Optional<Planilla> findByPeriodoPlanillaId(Long periodoPlanillaId);
    boolean existsByPeriodoPlanillaId(Long periodoPlanillaId);
}
