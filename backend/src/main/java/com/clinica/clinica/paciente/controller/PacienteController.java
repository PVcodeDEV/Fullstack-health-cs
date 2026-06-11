package com.clinica.clinica.paciente.controller;

import com.clinica.clinica.paciente.dto.PacienteRequest;
import com.clinica.clinica.paciente.dto.PacienteResponse;
import com.clinica.clinica.paciente.service.PacienteService;
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
@RequestMapping("/api/v1/pacientes")
@PreAuthorize("hasAuthority('paciente:*')")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<PacienteResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PacienteResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> create(@Valid @RequestBody PacienteRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public PacienteResponse update(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public PacienteResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
