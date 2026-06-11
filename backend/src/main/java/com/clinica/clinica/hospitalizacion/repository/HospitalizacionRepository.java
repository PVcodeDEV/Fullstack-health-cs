package com.clinica.clinica.hospitalizacion.repository;

import com.clinica.clinica.hospitalizacion.entity.Hospitalizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalizacionRepository extends JpaRepository<Hospitalizacion, Long> {

    Optional<Hospitalizacion> findBySolicitudId(Long solicitudId);

    List<Hospitalizacion> findByPacienteId(Long pacienteId);

    List<Hospitalizacion> findAllByCuentaId(Long cuentaId);

    Optional<Hospitalizacion> findByCuentaId(Long cuentaId);

    List<Hospitalizacion> findByEstado(String estado);

    List<Hospitalizacion> findAllByActivoTrue();
}
