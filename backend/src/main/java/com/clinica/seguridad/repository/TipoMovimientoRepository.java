package com.clinica.seguridad.repository;

import com.clinica.seguridad.entity.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoMovimientoRepository extends JpaRepository<TipoMovimiento, Long> {

    Optional<TipoMovimiento> findByCodigo(String codigo);

    List<TipoMovimiento> findByModulo(String modulo);

    boolean existsByCodigo(String codigo);
}
