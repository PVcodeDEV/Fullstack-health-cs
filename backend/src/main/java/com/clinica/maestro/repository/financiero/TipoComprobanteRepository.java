package com.clinica.maestro.repository.financiero;

import com.clinica.maestro.entity.financiero.TipoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoComprobanteRepository extends JpaRepository<TipoComprobante, Integer> {

    List<TipoComprobante> findAllByOrderByCodigoSunatAsc();

    Optional<TipoComprobante> findByCodigoSunat(String codigoSunat);

    boolean existsByCodigoSunat(String codigoSunat);
}
