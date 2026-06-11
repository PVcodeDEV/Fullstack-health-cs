package com.clinica.rrhh.planilla.controller;

import com.clinica.rrhh.planilla.dto.PeriodoPlanillaRequest;
import com.clinica.rrhh.planilla.dto.PeriodoPlanillaResponse;
import com.clinica.rrhh.planilla.service.PeriodoPlanillaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/periodos-planilla")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class PeriodoPlanillaController {

    private final PeriodoPlanillaService service;

    public PeriodoPlanillaController(PeriodoPlanillaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PeriodoPlanillaResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PeriodoPlanillaResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<PeriodoPlanillaResponse> create(@Valid @RequestBody PeriodoPlanillaRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public PeriodoPlanillaResponse cerrar(@PathVariable Long id) {
        return service.cerrar(id);
    }
}
