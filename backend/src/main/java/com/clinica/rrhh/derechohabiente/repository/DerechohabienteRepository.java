package com.clinica.rrhh.derechohabiente.repository;

import com.clinica.rrhh.derechohabiente.entity.Derechohabiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DerechohabienteRepository extends JpaRepository<Derechohabiente, Long> {
    List<Derechohabiente> findByTrabajadorIdAndEstadoOrderByFechaInicioDesc(Long trabajadorId, String estado);
    List<Derechohabiente> findByTrabajadorIdOrderByFechaInicioDesc(Long trabajadorId);
    List<Derechohabiente> findByEstado(String estado);
}
