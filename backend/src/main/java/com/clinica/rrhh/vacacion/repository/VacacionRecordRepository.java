package com.clinica.rrhh.vacacion.repository;

import com.clinica.rrhh.vacacion.entity.VacacionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VacacionRecordRepository extends JpaRepository<VacacionRecord, Long> {
    List<VacacionRecord> findByTrabajadorIdOrderByFechaInicioDesc(Long trabajadorId);
    Optional<VacacionRecord> findByTrabajadorIdAndFechaInicio(Long trabajadorId, LocalDate fechaInicio);
    List<VacacionRecord> findByEstado(String estado);
    boolean existsByTrabajadorIdAndFechaInicio(Long trabajadorId, LocalDate fechaInicio);
}
