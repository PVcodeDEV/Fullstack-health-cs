package com.clinica.caja.tarifario.repository;

import com.clinica.caja.tarifario.entity.TarifarioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifarioItemRepository extends JpaRepository<TarifarioItem, Long> {

    boolean existsByCodigo(String codigo);

    Optional<TarifarioItem> findByCodigo(String codigo);

    List<TarifarioItem> findByTarifarioIdAndActivoTrue(Long tarifarioId);

    /**
     * Find items effective on a specific date: fechaDesde <= date AND (fechaHasta IS NULL OR fechaHasta >= date).
     */
    @Query("""
        SELECT i FROM TarifarioItem i
        WHERE i.codigo = :codigo
        AND i.fechaDesde <= :fecha
        AND (i.fechaHasta IS NULL OR i.fechaHasta >= :fecha)
        AND i.activo = true
        ORDER BY i.fechaDesde DESC
    """)
    Optional<TarifarioItem> findEffectiveByCodigoAndFecha(
        @Param("codigo") String codigo,
        @Param("fecha") LocalDate fecha
    );

    /**
     * Find all price revisions for a given codigo, ordered by fechaDesde descending.
     */
    @Query("""
        SELECT i FROM TarifarioItem i
        WHERE i.codigo = :codigo
        AND i.activo = true
        ORDER BY i.fechaDesde DESC
    """)
    List<TarifarioItem> findHistoryByCodigo(@Param("codigo") String codigo);

    /**
     * Find active item by codigo (fechaHasta IS NULL = current active price).
     */
    @Query("""
        SELECT i FROM TarifarioItem i
        WHERE i.codigo = :codigo
        AND i.fechaHasta IS NULL
        AND i.activo = true
    """)
    Optional<TarifarioItem> findCurrentActiveByCodigo(@Param("codigo") String codigo);
}
