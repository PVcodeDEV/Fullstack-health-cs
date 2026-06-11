package com.clinica.rrhh.planilla.controller;

import com.clinica.rrhh.planilla.dto.PlanillaDetalleResponse;
import com.clinica.rrhh.planilla.dto.PlanillaResponse;
import com.clinica.rrhh.planilla.repository.PlanillaDetalleRepository;
import com.clinica.rrhh.planilla.repository.PlanillaRepository;
import com.clinica.rrhh.planilla.service.PlanillaLiquidacionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/planillas")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class PlanillaController {

    private final PlanillaRepository planillaRepository;
    private final PlanillaDetalleRepository planillaDetalleRepository;
    private final PlanillaLiquidacionService liquidacionService;

    public PlanillaController(PlanillaRepository planillaRepository,
                               PlanillaDetalleRepository planillaDetalleRepository,
                               PlanillaLiquidacionService liquidacionService) {
        this.planillaRepository = planillaRepository;
        this.planillaDetalleRepository = planillaDetalleRepository;
        this.liquidacionService = liquidacionService;
    }

    @GetMapping
    public List<PlanillaResponse> findAll() {
        return planillaRepository.findAllByOrderByPeriodoPlanillaAnioDescPeriodoPlanillaMesDesc()
            .stream().map(PlanillaResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public PlanillaResponse findById(@PathVariable Long id) {
        return planillaRepository.findById(id)
            .map(PlanillaResponse::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Planilla no encontrada: " + id));
    }

    @GetMapping("/{id}/detalles")
    public List<PlanillaDetalleResponse> findDetalles(@PathVariable Long id) {
        return planillaDetalleRepository.findByPlanillaId(id)
            .stream().map(PlanillaDetalleResponse::fromEntity).toList();
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<PlanillaResponse> generar(@RequestParam Long periodoPlanillaId) {
        var response = liquidacionService.generar(periodoPlanillaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
