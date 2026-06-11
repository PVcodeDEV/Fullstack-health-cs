package com.clinica.rrhh.cts.controller;

import com.clinica.rrhh.cts.dto.DepositoCtsResponse;
import com.clinica.rrhh.cts.service.CtsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cts")
@PreAuthorize("hasAuthority('rrhh:ver')")
public class CtsController {

    private final CtsService service;

    public CtsController(CtsService service) {
        this.service = service;
    }

    @PostMapping("/calcular")
    @PreAuthorize("hasAuthority('rrhh:editar')")
    public ResponseEntity<List<DepositoCtsResponse>> calcular(@RequestParam Long periodoPlanillaId) {
        var resultados = service.calcular(periodoPlanillaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultados);
    }

    @GetMapping
    public List<DepositoCtsResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public DepositoCtsResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
