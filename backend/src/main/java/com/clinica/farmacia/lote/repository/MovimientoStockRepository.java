package com.clinica.farmacia.lote.repository;

import com.clinica.farmacia.lote.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    List<MovimientoStock> findByLoteIdOrderByCreatedAtDesc(Long loteId);
}
