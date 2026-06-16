package com.clinica.caja.comprobante.repository;

import com.clinica.caja.comprobante.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    /**
     * Find the highest correlativo for a given series.
     * Used for auto-increment: max numeric value + 1.
     */
    @Query("SELECT MAX(c.correlativo) FROM Comprobante c WHERE c.serie = :serie")
    Optional<String> findMaxCorrelativoBySerie(@Param("serie") String serie);

    @Query("SELECT c FROM Comprobante c WHERE c.fechaEmision >= :fecha ORDER BY c.fechaEmision DESC")
    List<Comprobante> findByFechaEmisionAfter(@Param("fecha") LocalDateTime fecha);
}
