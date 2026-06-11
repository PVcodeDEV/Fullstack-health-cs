package com.clinica.maestro.repository.rrhh;

import com.clinica.maestro.entity.rrhh.ConceptoPlanilla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConceptoPlanillaRepository extends JpaRepository<ConceptoPlanilla, Long> {
    List<ConceptoPlanilla> findAllByActivoTrueOrderByOrden();
    Optional<ConceptoPlanilla> findByCodigo(String codigo);
}
