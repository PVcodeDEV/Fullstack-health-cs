package com.clinica.caja.tarifario.repository;

import com.clinica.caja.tarifario.entity.PaqueteDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaqueteDetalleRepository extends JpaRepository<PaqueteDetalle, Long> {

    List<PaqueteDetalle> findByPaqueteId(Long paqueteId);

    boolean existsByTarifarioItemId(Long tarifarioItemId);
}
