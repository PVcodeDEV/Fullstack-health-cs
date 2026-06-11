package com.clinica.clinica.hospitalizacion.repository;

import com.clinica.clinica.hospitalizacion.entity.AltaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AltaMedicaRepository extends JpaRepository<AltaMedica, Long> {

    Optional<AltaMedica> findByHospitalizacionId(Long hospitalizacionId);
}
