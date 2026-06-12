package com.clinica.entidad.repository;

import com.clinica.entidad.entity.Empresa;
import com.clinica.entidad.entity.Empresa.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    List<Empresa> findAllByActivoTrueOrderByRazonSocial();

    Optional<Empresa> findByRucAndActivoTrue(String ruc);

    /**
     * Search by RUC or razonSocial (case-insensitive LIKE).
     */
    @Query("""
        SELECT e FROM Empresa e
        WHERE e.activo = true
        AND (:q IS NULL OR LOWER(e.ruc) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(e.razonSocial) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<Empresa> search(@Param("q") String q, Pageable pageable);

    /**
     * Filter by rol and/or estado with optional search term.
     */
    @Query("""
        SELECT e FROM Empresa e
        WHERE (:rol IS NULL OR e.rol = :rol)
        AND (:estado IS NULL OR e.estado = :estado)
        AND (:q IS NULL OR LOWER(e.ruc) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(e.razonSocial) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<Empresa> findByFilters(
        @Param("rol") Rol rol,
        @Param("estado") Empresa.Estado estado,
        @Param("q") String q,
        Pageable pageable
    );

    List<Empresa> findByPersonaId(Long personaId);
}
