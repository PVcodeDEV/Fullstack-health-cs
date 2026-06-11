package com.clinica.farmacia.reposicion.repository;

import com.clinica.farmacia.reposicion.entity.Reposicion;
import com.clinica.farmacia.reposicion.type.EstadoReposicion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Reposicion} — replenishment planning.
 */
@Repository
public interface ReposicionRepository extends JpaRepository<Reposicion, Long> {

    List<Reposicion> findByEstadoOrderByGeneradaEnDesc(EstadoReposicion estado);

    Page<Reposicion> findByEstado(EstadoReposicion estado, Pageable pageable);

    long countByEstado(EstadoReposicion estado);
}
