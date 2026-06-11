package com.clinica.clinica.cama.controller;

import com.clinica.clinica.cama.dto.CamaRequest;
import com.clinica.clinica.cama.dto.CamaResponse;
import com.clinica.clinica.cama.service.CamaService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/camas")
@PreAuthorize("hasAuthority('cama:ver')")
public class CamaController {

    private final CamaService service;

    public CamaController(CamaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CamaResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CamaResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/habitacion/{habitacionId}")
    public List<CamaResponse> findByHabitacionId(@PathVariable Long habitacionId) {
        return service.findByHabitacionId(habitacionId);
    }

    @GetMapping("/disponibles")
    public List<CamaResponse> findDisponibles() {
        return service.findDisponibles();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cama:editar')")
    public ResponseEntity<CamaResponse> create(@Valid @RequestBody CamaRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('cama:editar')")
    public CamaResponse cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return service.cambiarEstado(id, body.get("estado"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cama:editar')")
    public CamaResponse update(@PathVariable Long id, @Valid @RequestBody CamaRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cama:editar')")
    public CamaResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
