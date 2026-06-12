package com.clinica.caja.tipocambio.repository;

import com.clinica.caja.tipocambio.entity.TipoCambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoCambioRepository extends JpaRepository<TipoCambio, Long> {

    /**
     * Find the latest exchange rate for a given currency pair.
     */
    @Query("""
        SELECT t FROM TipoCambio t
        WHERE t.monedaOrigen = :origen
        AND t.monedaDestino = :destino
        AND t.activo = true
        ORDER BY t.fecha DESC, t.id DESC
    """)
    Optional<TipoCambio> findLatestByMonedas(
        @Param("origen") String monedaOrigen,
        @Param("destino") String monedaDestino
    );
}
