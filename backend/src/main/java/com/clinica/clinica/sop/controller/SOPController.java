package com.clinica.clinica.sop.controller;

import com.clinica.clinica.sop.dto.ReporteQuirurgicoRequest;
import com.clinica.clinica.sop.dto.ReporteQuirurgicoResponse;
import com.clinica.clinica.sop.dto.URPARegistroRequest;
import com.clinica.clinica.sop.dto.URPARegistroResponse;
import com.clinica.clinica.sop.service.SOPService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sop")
@PreAuthorize("hasAuthority('sop:ver')")
public class SOPController {

    private final SOPService service;

    public SOPController(SOPService service) {
        this.service = service;
    }

    @PostMapping("/reportes")
    @PreAuthorize("hasAuthority('sop:editar')")
    public ResponseEntity<ReporteQuirurgicoResponse> crearReporte(@Valid @RequestBody ReporteQuirurgicoRequest request) {
        var response = service.crearReporte(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/reportes/{id}/completar")
    @PreAuthorize("hasAuthority('sop:editar')")
    public ReporteQuirurgicoResponse completarReporte(@PathVariable Long id) {
        return service.completarReporte(id);
    }

    @PostMapping("/reportes/{id}/urpa")
    @PreAuthorize("hasAuthority('sop:editar')")
    public ResponseEntity<URPARegistroResponse> registrarURPA(
            @PathVariable Long id,
            @Valid @RequestBody URPARegistroRequest request) {
        var response = service.registrarURPA(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
