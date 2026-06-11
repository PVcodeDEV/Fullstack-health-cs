package com.clinica.rrhh.planilla.repository;

import com.clinica.rrhh.planilla.entity.PlanillaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanillaDetalleRepository extends JpaRepository<PlanillaDetalle, Long> {
    List<PlanillaDetalle> findByPlanillaId(Long planillaId);
}
