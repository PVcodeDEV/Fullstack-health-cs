package com.clinica.rrhh.planillaplame.repository;

import com.clinica.rrhh.planillaplame.entity.TRegistroEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TRegistroEventoRepository extends JpaRepository<TRegistroEvento, Long> {

    List<TRegistroEvento> findByPeriodoPlanillaIdOrderByFechaEventoAsc(Long periodoPlanillaId);

    List<TRegistroEvento> findByTrabajadorIdOrderByFechaEventoDesc(Long trabajadorId);

    List<TRegistroEvento> findByTipoEvento(String tipoEvento);

    boolean existsByPeriodoPlanillaId(Long periodoPlanillaId);
}
