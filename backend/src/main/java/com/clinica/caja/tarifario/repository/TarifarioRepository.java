package com.clinica.caja.tarifario.repository;

import com.clinica.caja.tarifario.entity.Tarifario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifarioRepository extends JpaRepository<Tarifario, Long> {

    List<Tarifario> findAllByActivoTrue();

    Optional<Tarifario> findByAseguradoraIdAndActivoTrue(Long aseguradoraId);

    List<Tarifario> findByAseguradoraIdIsNullAndActivoTrue();
}
