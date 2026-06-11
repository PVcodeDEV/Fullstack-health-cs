package com.clinica.maestro.repository.financiero;

import com.clinica.maestro.entity.financiero.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {

    List<UnidadMedida> findAllByOrderByCodigoSunatAsc();

    Optional<UnidadMedida> findByCodigoSunat(String codigoSunat);

    boolean existsByCodigoSunat(String codigoSunat);
}
