package com.clinica.farmacia.caja.repository;

import com.clinica.farmacia.caja.entity.SesionCaja;
import com.clinica.farmacia.caja.type.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link SesionCaja} — cash session management.
 */
@Repository
public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    /**
     * Find the current open session for a cajero, latest first.
     */
    Optional<SesionCaja> findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(
        Long usuarioId, EstadoSesion estado);

    /**
     * Check if a user already has a session in the given state.
     * Used to prevent opening multiple sessions.
     */
    boolean existsByUsuarioIdAndEstado(Long usuarioId, EstadoSesion estado);

    /**
     * List sessions by state, ordered by latest first.
     */
    List<SesionCaja> findByEstadoOrderByFechaAperturaDesc(EstadoSesion estado);

    /**
     * List sessions within a date range for reports.
     */
    List<SesionCaja> findByFechaAperturaBetweenOrderByFechaAperturaDesc(
        LocalDateTime desde, LocalDateTime hasta);
}
