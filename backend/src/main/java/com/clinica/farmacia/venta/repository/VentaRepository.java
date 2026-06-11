package com.clinica.farmacia.venta.repository;

import com.clinica.farmacia.venta.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Check if a correlativo already exists for a given session.
     */
    boolean existsBySesionCajaIdAndCorrelativo(Long sesionCajaId, Integer correlativo);

    /**
     * Find all sales for a cash session, ordered by correlativo ascending.
     */
    List<Venta> findBySesionCajaIdOrderByCorrelativoAsc(Long sesionCajaId);

    /**
     * Find the maximum correlativo for a given session.
     */
    @Query("SELECT COALESCE(MAX(v.correlativo), 0) FROM Venta v WHERE v.sesionCaja.id = :sesionCajaId")
    Integer findMaxCorrelativoBySesionCajaId(@Param("sesionCajaId") Long sesionCajaId);

    /**
     * Find all sales for a client, ordered by creation date descending.
     */
    List<Venta> findByClientePersonaIdOrderByCreatedAtDesc(Long clientePersonaId);

    /**
     * Find by id with eager fetching of detalles.
     */
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalles WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Long id);
}
