package com.clinica.rrhh.gratificacion.controller;

import com.clinica.rrhh.gratificacion.dto.GratificacionResponse;
import com.clinica.rrhh.gratificacion.service.GratificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gratificaciones")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class GratificacionController {

    private final GratificacionService service;

    public GratificacionController(GratificacionService service) {
        this.service = service;
    }

    @PostMapping("/calcular")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<List<GratificacionResponse>> calcular(@RequestParam Long periodoPlanillaId) {
        var resultados = service.calcular(periodoPlanillaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultados);
    }

    @GetMapping
    public List<GratificacionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GratificacionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
