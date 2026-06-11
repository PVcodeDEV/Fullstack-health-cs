package com.clinica.rrhh.periodo.controller;

import com.clinica.rrhh.periodo.dto.CeseRequest;
import com.clinica.rrhh.periodo.dto.PeriodoLaboralResponse;
import com.clinica.rrhh.periodo.service.PeriodoLaboralService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/periodos")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class PeriodoLaboralController {

    private final PeriodoLaboralService periodoLaboralService;

    public PeriodoLaboralController(PeriodoLaboralService periodoLaboralService) {
        this.periodoLaboralService = periodoLaboralService;
    }

    @GetMapping("/{id}")
    public PeriodoLaboralResponse findById(@PathVariable Long id) {
        return periodoLaboralService.findById(id);
    }

    @PutMapping("/{id}/cese")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public PeriodoLaboralResponse registrarCese(
            @PathVariable Long id,
            @RequestBody @Valid CeseRequest request) {
        return periodoLaboralService.registrarCese(id, request.fechaCese(), request.motivo());
    }
}
