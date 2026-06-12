package com.clinica.caja.sesion.repository;

import com.clinica.caja.sesion.entity.SesionCaja;
import com.clinica.caja.sesion.entity.SesionCaja.Estado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for clinical {@link SesionCaja}.
 * Named {@code ClinicaSesionCajaRepository} to avoid bean name clash with
 * {@code com.clinica.farmacia.caja.repository.SesionCajaRepository}.
 * JPQL queries use {@code ClinicaSesionCaja} entity name (see {@link SesionCaja}).
 */
@Repository
public interface ClinicaSesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    /**
     * Find an active open session for a given user.
     */
    Optional<SesionCaja> findByUsuarioAperturaIdAndEstadoAndActivoTrue(Long usuarioAperturaId, Estado estado);

    /**
     * Check if user has an open session.
     */
    boolean existsByUsuarioAperturaIdAndEstadoAndActivoTrue(Long usuarioAperturaId, Estado estado);

    /**
     * Find sessions by date range, ordered by fechaApertura descending.
     */
    @Query("""
        SELECT s FROM ClinicaSesionCaja s
        WHERE (:fechaDesde IS NULL OR s.fechaApertura >= :fechaDesde)
        AND (:fechaHasta IS NULL OR s.fechaApertura <= :fechaHasta)
        AND (:estado IS NULL OR s.estado = :estado)
        ORDER BY s.fechaApertura DESC
    """)
    Page<SesionCaja> findByFilters(
        @Param("fechaDesde") LocalDateTime fechaDesde,
        @Param("fechaHasta") LocalDateTime fechaHasta,
        @Param("estado") Estado estado,
        Pageable pageable
    );
}
