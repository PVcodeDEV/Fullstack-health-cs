package com.clinica.clinica.admision.controller;

import com.clinica.clinica.admision.dto.AdmisionDiagnosticoRequest;
import com.clinica.clinica.admision.dto.AdmisionDiagnosticoResponse;
import com.clinica.clinica.admision.dto.AsignarCamaRequest;
import com.clinica.clinica.admision.dto.CuentaRequest;
import com.clinica.clinica.admision.dto.CuentaResponse;
import com.clinica.clinica.admision.service.AdmisionService;
import com.clinica.persona.dto.PersonaSearchResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admision")
@PreAuthorize("hasAuthority('admision:ver')")
public class AdmisionController {

    private final AdmisionService service;

    public AdmisionController(AdmisionService service) {
        this.service = service;
    }

    @GetMapping("/pacientes")
    public List<PersonaSearchResponse> buscarPaciente(@RequestParam String query) {
        return service.buscarPaciente(query).stream()
                .map(PersonaSearchResponse::fromEntity)
                .toList();
    }

    @PostMapping("/cuentas")
    @PreAuthorize("hasAuthority('admision:editar')")
    public ResponseEntity<CuentaResponse> crearCuenta(@Valid @RequestBody CuentaRequest request) {
        var response = service.crearCuenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/cuentas/{id}/diagnosticos")
    @PreAuthorize("hasAuthority('admision:editar')")
    public ResponseEntity<AdmisionDiagnosticoResponse> registrarDiagnostico(
            @PathVariable Long id,
            @Valid @RequestBody AdmisionDiagnosticoRequest request) {
        var response = service.registrarDiagnostico(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/cuentas/{id}/asignar-cama")
    @PreAuthorize("hasAuthority('admision:editar')")
    public ResponseEntity<Object> asignarCama(
            @PathVariable Long id,
            @Valid @RequestBody AsignarCamaRequest request) {
        var response = service.asignarCama(request);
        return ResponseEntity.ok(response);
    }
}
