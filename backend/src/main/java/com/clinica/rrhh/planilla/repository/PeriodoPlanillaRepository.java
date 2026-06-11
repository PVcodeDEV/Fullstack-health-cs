package com.clinica.rrhh.planilla.repository;

import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PeriodoPlanillaRepository extends JpaRepository<PeriodoPlanilla, Long> {
    Optional<PeriodoPlanilla> findByAnioAndMes(Integer anio, Integer mes);
    List<PeriodoPlanilla> findAllByOrderByAnioDescMesDesc();
    boolean existsByAnioAndMes(Integer anio, Integer mes);
}
