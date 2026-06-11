package com.clinica.clinica.paciente.repository;

import com.clinica.clinica.paciente.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByPersonaId(Long personaId);

    Optional<Paciente> findByNroHistoriaClinica(String nroHistoriaClinica);

    List<Paciente> findAllByActivoTrue();

    boolean existsByPersonaId(Long personaId);
}
