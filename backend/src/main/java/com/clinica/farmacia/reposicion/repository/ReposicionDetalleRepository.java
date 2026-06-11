package com.clinica.farmacia.reposicion.repository;

import com.clinica.farmacia.reposicion.entity.ReposicionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link ReposicionDetalle}.
 */
@Repository
public interface ReposicionDetalleRepository extends JpaRepository<ReposicionDetalle, Long> {

    List<ReposicionDetalle> findByReposicionId(Long reposicionId);
}
