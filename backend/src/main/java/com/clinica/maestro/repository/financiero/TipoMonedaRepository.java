package com.clinica.maestro.repository.financiero;

import com.clinica.maestro.entity.financiero.TipoMoneda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoMonedaRepository extends JpaRepository<TipoMoneda, Integer> {

    List<TipoMoneda> findAllByOrderByCodigoSunatAsc();

    Optional<TipoMoneda> findByCodigoSunat(String codigoSunat);

    boolean existsByCodigoSunat(String codigoSunat);
}
