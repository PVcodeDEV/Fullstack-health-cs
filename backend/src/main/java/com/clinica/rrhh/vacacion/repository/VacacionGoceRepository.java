package com.clinica.rrhh.vacacion.repository;

import com.clinica.rrhh.vacacion.entity.VacacionGoce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacacionGoceRepository extends JpaRepository<VacacionGoce, Long> {
    List<VacacionGoce> findByRecordIdOrderByFechaInicioAsc(Long recordId);
    List<VacacionGoce> findByRecordIdAndEstado(Long recordId, String estado);
    List<VacacionGoce> findByEstado(String estado);
    long countByRecordIdAndEstado(Long recordId, String estado);
    List<VacacionGoce> findByEstadoAndFechaFinBetween(String estado, LocalDate start, LocalDate end);
}
