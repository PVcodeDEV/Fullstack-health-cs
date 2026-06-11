package com.clinica.farmacia.lote.repository;

import com.clinica.farmacia.lote.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByProductoIdAndStockActualGreaterThanAndActivoTrue(Long productoId, Integer stockActual);

    List<Lote> findByAlmacenIdAndProductoIdAndStockActualGreaterThanAndActivoTrue(
        Long almacenId, Long productoId, Integer stockActual);

    List<Lote> findByFechaVencimientoBeforeAndStockActualGreaterThanAndActivoTrue(
        LocalDate fecha, Integer stockActual);

    boolean existsByAlmacenIdAndStockActualGreaterThanAndActivoTrue(Long almacenId, Integer stockActual);

    boolean existsByProductoIdAndStockActualGreaterThan(Long productoId, Integer stockActual);
}
