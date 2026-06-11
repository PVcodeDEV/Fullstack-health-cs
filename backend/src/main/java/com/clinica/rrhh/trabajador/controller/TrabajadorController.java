package com.clinica.rrhh.trabajador.controller;

import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.service.ContratoService;
import com.clinica.rrhh.periodo.dto.PeriodoLaboralResponse;
import com.clinica.rrhh.periodo.service.PeriodoLaboralService;
import com.clinica.rrhh.trabajador.dto.ReingresoRequest;
import com.clinica.rrhh.trabajador.dto.TrabajadorRequest;
import com.clinica.rrhh.trabajador.dto.TrabajadorResponse;
import com.clinica.rrhh.trabajador.service.TrabajadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trabajadores")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class TrabajadorController {

    private final TrabajadorService service;
    private final ContratoService contratoService;
    private final PeriodoLaboralService periodoLaboralService;

    public TrabajadorController(TrabajadorService service,
                                ContratoService contratoService,
                                PeriodoLaboralService periodoLaboralService) {
        this.service = service;
        this.contratoService = contratoService;
        this.periodoLaboralService = periodoLaboralService;
    }

    @GetMapping
    public List<TrabajadorResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TrabajadorResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<TrabajadorResponse> create(@Valid @RequestBody TrabajadorRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public TrabajadorResponse update(@PathVariable Long id, @Valid @RequestBody TrabajadorRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public TrabajadorResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }

    @GetMapping("/{id}/contratos")
    public List<ContratoResponse> findContratos(@PathVariable Long id) {
        return contratoService.findByTrabajadorId(id);
    }

    @GetMapping("/{id}/periodos")
    public List<PeriodoLaboralResponse> findPeriodos(@PathVariable Long id) {
        return periodoLaboralService.findByTrabajadorId(id);
    }

    @PostMapping("/{id}/reingreso")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<PeriodoLaboralResponse> registrarReingreso(
            @PathVariable Long id,
            @RequestBody @Valid ReingresoRequest request) {
        var response = periodoLaboralService.registrarIngreso(id, request.fechaInicio(), true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
