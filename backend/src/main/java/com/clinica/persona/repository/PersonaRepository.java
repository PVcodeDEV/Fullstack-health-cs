package com.clinica.persona.repository;

import com.clinica.persona.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByNumeroDocumento(String numeroDocumento);

    List<Persona> findByNombresContainingIgnoreCase(String nombres);

    List<Persona> findByApellidoPaternoContainingIgnoreCase(String apellidoPaterno);

    List<Persona> findAllByActivoTrue();

    boolean existsByNumeroDocumento(String numeroDocumento);
}
