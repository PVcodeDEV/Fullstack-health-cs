package com.clinica.maestro.repository.clinico;

import com.clinica.maestro.entity.clinico.TipoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Long> {

    List<TipoHabitacion> findAllByOrderByNombreAsc();

    Optional<TipoHabitacion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
