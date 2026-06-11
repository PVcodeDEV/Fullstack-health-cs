package com.clinica.rrhh.periodo.repository;

import com.clinica.rrhh.periodo.entity.PeriodoLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodoLaboralRepository extends JpaRepository<PeriodoLaboral, Long> {
    List<PeriodoLaboral> findByTrabajadorIdOrderByFechaInicioDesc(Long trabajadorId);
    Optional<PeriodoLaboral> findByTrabajadorIdAndActivoTrue(Long trabajadorId);
}
