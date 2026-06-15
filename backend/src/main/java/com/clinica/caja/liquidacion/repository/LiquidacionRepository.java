package com.clinica.caja.liquidacion.repository;

import com.clinica.caja.liquidacion.entity.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {

    List<Liquidacion> findByCuentaId(Long cuentaId);

    List<Liquidacion> findBySesionId(Long sesionId);

    boolean existsByCuentaIdAndEstado(Long cuentaId, String estado);
}
