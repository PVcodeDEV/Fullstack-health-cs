package com.clinica.maestro.repository.farmacia;

import com.clinica.maestro.entity.farmacia.TipoMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoMedicamentoRepository extends JpaRepository<TipoMedicamento, Long> {

    List<TipoMedicamento> findAllByActivoTrueOrderByCodigo();

    Optional<TipoMedicamento> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
