package com.clinica.clinica.cama.repository;

import com.clinica.clinica.cama.entity.Cama;
import com.clinica.clinica.cama.entity.EstadoCama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CamaRepository extends JpaRepository<Cama, Long> {

    Optional<Cama> findByCodigo(String codigo);

    List<Cama> findByHabitacionId(Long habitacionId);

    List<Cama> findByEstado(EstadoCama estado);

    List<Cama> findByHabitacionIdAndEstado(Long habitacionId, EstadoCama estado);

    long countByHabitacionIdAndEstado(Long habitacionId, EstadoCama estado);

    List<Cama> findAllByActivoTrue();
}
