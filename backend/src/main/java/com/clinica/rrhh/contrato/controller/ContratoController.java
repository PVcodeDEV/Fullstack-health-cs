package com.clinica.rrhh.contrato.controller;

import com.clinica.rrhh.contrato.dto.ContratoRequest;
import com.clinica.rrhh.contrato.dto.ContratoResponse;
import com.clinica.rrhh.contrato.dto.ContratoUpdateRequest;
import com.clinica.rrhh.contrato.dto.ResolverRequest;
import com.clinica.rrhh.contrato.service.ContratoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contratos")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping
    public List<ContratoResponse> findAll() {
        return contratoService.findAll();
    }

    @GetMapping("/{id}")
    public ContratoResponse findById(@PathVariable Long id) {
        return contratoService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<ContratoResponse> create(@Valid @RequestBody ContratoRequest request) {
        var response = contratoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ContratoResponse suspender(@PathVariable Long id) {
        return contratoService.suspender(id);
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ContratoResponse reactivar(@PathVariable Long id) {
        return contratoService.reactivar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ContratoResponse update(@PathVariable Long id, @Valid @RequestBody ContratoUpdateRequest request) {
        return contratoService.update(id, request);
    }

    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ContratoResponse resolver(@PathVariable Long id, @Valid @RequestBody ResolverRequest request) {
        return contratoService.resolver(id, request.motivoCese());
    }
}
