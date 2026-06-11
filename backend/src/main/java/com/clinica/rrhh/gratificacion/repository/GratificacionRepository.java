package com.clinica.rrhh.gratificacion.repository;

import com.clinica.rrhh.gratificacion.entity.Gratificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GratificacionRepository extends JpaRepository<Gratificacion, Long> {
    List<Gratificacion> findByPeriodoPlanillaId(Long periodoPlanillaId);
    Optional<Gratificacion> findByPeriodoPlanillaIdAndTrabajadorId(Long periodoPlanillaId, Long trabajadorId);
    List<Gratificacion> findByTrabajadorIdOrderByCreatedAtDesc(Long trabajadorId);
    boolean existsByPeriodoPlanillaId(Long periodoPlanillaId);
}
