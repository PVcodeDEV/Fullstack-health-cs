package com.clinica.maestro.repository.organizacion;

import com.clinica.maestro.entity.organizacion.Aseguradora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AseguradoraRepository extends JpaRepository<Aseguradora, Integer> {

    List<Aseguradora> findAllByOrderByNombreAsc();

    Optional<Aseguradora> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
