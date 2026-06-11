package com.clinica.maestro.repository.clinico;

import com.clinica.maestro.entity.clinico.TipoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoPacienteRepository extends JpaRepository<TipoPaciente, Long> {

    List<TipoPaciente> findAllByOrderByNombreAsc();

    Optional<TipoPaciente> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
