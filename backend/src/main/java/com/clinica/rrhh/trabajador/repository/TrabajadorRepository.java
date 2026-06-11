package com.clinica.rrhh.trabajador.repository;

import com.clinica.rrhh.trabajador.entity.Trabajador;
import com.clinica.rrhh.type.TipoTrabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {

    Optional<Trabajador> findByPersonaId(Long personaId);

    Optional<Trabajador> findByCodigoTrabajador(String codigoTrabajador);

    List<Trabajador> findAllByActivoTrue();

    boolean existsByPersonaId(Long personaId);

    boolean existsByCodigoTrabajador(String codigoTrabajador);

    List<Trabajador> findByTipo(TipoTrabajador tipo);
}
