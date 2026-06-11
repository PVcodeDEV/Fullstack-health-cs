package com.clinica.rrhh.contrato.repository;

import com.clinica.rrhh.contrato.entity.Contrato;
import com.clinica.rrhh.type.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    List<Contrato> findByTrabajadorIdOrderByFechaInicioDesc(Long trabajadorId);
    Optional<Contrato> findByTrabajadorIdAndEstado(Long trabajadorId, EstadoContrato estado);
    List<Contrato> findByEstado(EstadoContrato estado);
    List<Contrato> findByFechaInicioBetween(LocalDate start, LocalDate end);
    List<Contrato> findByFechaFinBetween(LocalDate start, LocalDate end);
    List<Contrato> findByEstadoAndUpdatedAtBetween(EstadoContrato estado, LocalDateTime start, LocalDateTime end);
}
