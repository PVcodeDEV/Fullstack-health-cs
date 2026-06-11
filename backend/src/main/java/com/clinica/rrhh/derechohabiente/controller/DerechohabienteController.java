package com.clinica.rrhh.derechohabiente.controller;

import com.clinica.rrhh.derechohabiente.dto.DerechohabienteRequest;
import com.clinica.rrhh.derechohabiente.dto.DerechohabienteResponse;
import com.clinica.rrhh.derechohabiente.service.DerechohabienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/derechohabientes")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class DerechohabienteController {

    private final DerechohabienteService service;

    public DerechohabienteController(DerechohabienteService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public DerechohabienteResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public List<DerechohabienteResponse> findByTrabajadorId(@PathVariable Long trabajadorId) {
        return service.findByTrabajadorId(trabajadorId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<DerechohabienteResponse> create(@Valid @RequestBody DerechohabienteRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/inactivar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public DerechohabienteResponse inactivar(@PathVariable Long id) {
        return service.inactivar(id);
    }
}
