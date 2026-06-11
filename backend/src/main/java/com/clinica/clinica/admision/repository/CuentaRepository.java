package com.clinica.clinica.admision.repository;

import com.clinica.clinica.admision.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByPacienteId(Long pacienteId);

    List<Cuenta> findByEstado(String estado);

    List<Cuenta> findAllByActivoTrue();
}
