package com.clinica.clinica.cama.repository;

import com.clinica.clinica.cama.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    Optional<Habitacion> findByCodigo(String codigo);

    List<Habitacion> findByTipoHabitacionId(Long tipoHabitacionId);

    List<Habitacion> findAllByActivoTrue();
}
