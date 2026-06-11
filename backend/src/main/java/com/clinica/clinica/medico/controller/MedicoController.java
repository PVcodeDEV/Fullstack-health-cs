package com.clinica.clinica.medico.controller;

import com.clinica.clinica.medico.dto.MedicoRequest;
import com.clinica.clinica.medico.dto.MedicoResponse;
import com.clinica.clinica.medico.service.MedicoService;
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
@RequestMapping("/api/v1/medicos")
@PreAuthorize("hasAuthority('medico:*')")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MedicoResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MedicoResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<MedicoResponse> create(@Valid @RequestBody MedicoRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public MedicoResponse update(@PathVariable Long id, @Valid @RequestBody MedicoRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MedicoResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
