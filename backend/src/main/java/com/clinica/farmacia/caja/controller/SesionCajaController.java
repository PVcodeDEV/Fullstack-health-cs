package com.clinica.farmacia.caja.controller;

import com.clinica.farmacia.caja.dto.SesionCajaAbrirRequest;
import com.clinica.farmacia.caja.dto.SesionCajaCerrarRequest;
import com.clinica.farmacia.caja.dto.SesionCajaResponse;
import com.clinica.farmacia.caja.service.SesionCajaService;
import com.clinica.farmacia.caja.type.EstadoSesion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for cash session management.
 * Implements CAJ-02, CAJ-03, SC-13, SC-14, SC-15.
 */
@RestController
@RequestMapping("/api/v1/farmacia/caja/sesiones")
@PreAuthorize("hasAnyRole('QUIMICO', 'TECNICO', 'ENCARGADO_SISTEMA')")
public class SesionCajaController {

    private final SesionCajaService service;

    public SesionCajaController(SesionCajaService service) {
        this.service = service;
    }

    /**
     * Open a new cash session for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<SesionCajaResponse> abrir(
            @Valid @RequestBody SesionCajaAbrirRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        var response = service.abrir(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Close an open cash session.
     */
    @PostMapping("/{id}/cerrar")
    public SesionCajaResponse cerrar(
            @PathVariable Long id,
            @Valid @RequestBody SesionCajaCerrarRequest request) {
        return service.cerrar(id, request);
    }

    /**
     * Get the current open session for the authenticated user.
     */
    @GetMapping("/abierta")
    public ResponseEntity<SesionCajaResponse> findAbierta(Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        return service.findOpenByUsuario(usuarioId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List sessions, optionally filtered by estado.
     */
    @GetMapping
    public List<SesionCajaResponse> list(
            @RequestParam(required = false) String estado) {
        if (estado != null) {
            return service.listByEstado(EstadoSesion.valueOf(estado));
        }
        return service.listAll();
    }

    /**
     * Get a session by ID.
     */
    @GetMapping("/{id}")
    public SesionCajaResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * Extract user ID from the Authentication principal.
     * In v1, the JWT subject contains the user ID as a Long.
     * Falls back to 0L if parsing fails.
     */
    private Long extractUsuarioId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return 0L;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
