package com.clinica.farmacia.lote.controller;

import com.clinica.farmacia.lote.dto.LoteRequest;
import com.clinica.farmacia.lote.dto.LoteResponse;
import com.clinica.farmacia.lote.service.LoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farmacia/lotes")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @PostMapping("/recibir")
    @PreAuthorize("hasAnyRole('QUIMICO','TECNICO','ENCARGADO_SISTEMA')")
    public ResponseEntity<LoteResponse> recibir(@Valid @RequestBody LoteRequest request) {
        var response = loteService.recibir(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('QUIMICO','TECNICO','ENCARGADO_SISTEMA')")
    public List<LoteResponse> findByProducto(@PathVariable Long productoId) {
        return loteService.findByProducto(productoId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('QUIMICO','TECNICO','ENCARGADO_SISTEMA')")
    public LoteResponse findById(@PathVariable Long id) {
        return loteService.findById(id);
    }
}
