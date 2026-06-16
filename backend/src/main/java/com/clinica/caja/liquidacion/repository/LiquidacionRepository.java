package com.clinica.caja.liquidacion.repository;

import com.clinica.caja.liquidacion.entity.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {

    List<Liquidacion> findByCuentaId(Long cuentaId);

    List<Liquidacion> findBySesionId(Long sesionId);

    boolean existsByCuentaIdAndEstado(Long cuentaId, String estado);

    @Query("SELECT l FROM Liquidacion l WHERE l.usuarioCobraId = :usuarioId ORDER BY l.fecha DESC")
    List<Liquidacion> findRecentByUsuarioCobraId(@Param("usuarioId") Long usuarioId, @Param("limit") int limit);

    @Query("SELECT l FROM Liquidacion l WHERE l.fecha >= :fecha ORDER BY l.fecha DESC")
    List<Liquidacion> findByFechaAfter(@Param("fecha") LocalDateTime fecha);
}
