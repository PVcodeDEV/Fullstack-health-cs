package com.clinica.maestro.repository.farmacia;

import com.clinica.maestro.entity.farmacia.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    List<Marca> findAllByActivoTrueOrderByCodigo();

    Optional<Marca> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
