package com.clinica.clinica.medico.repository;

import com.clinica.clinica.medico.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByPersonaId(Long personaId);

    Optional<Medico> findByTrabajadorId(Long trabajadorId);

    Optional<Medico> findByCmp(String cmp);

    List<Medico> findAllByActivoTrue();

    boolean existsByPersonaId(Long personaId);

    boolean existsByTrabajadorId(Long trabajadorId);

    boolean existsByCmp(String cmp);
}
