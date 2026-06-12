package com.clinica.caja.sesion.controller;

import com.clinica.caja.sesion.dto.SesionCajaCerrarRequest;
import com.clinica.caja.sesion.dto.SesionCajaRequest;
import com.clinica.caja.sesion.dto.SesionCajaResponse;
import com.clinica.caja.sesion.service.SesionCajaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController("clinicaSesionCajaController")
@RequestMapping("/api/v1/caja/sesion")
public class SesionCajaController {

    private final SesionCajaService sesionCajaService;
    private final Clock clock;

    public SesionCajaController(SesionCajaService sesionCajaService, Clock clock) {
        this.sesionCajaService = sesionCajaService;
        this.clock = clock;
    }

    /**
     * Open a new cash session.
     */
    @PostMapping("/abrir")
    @PreAuthorize("hasAuthority('caja:crear')")
    public ResponseEntity<SesionCajaResponse> abrirSesion(
            @Valid @RequestBody SesionCajaRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        var response = sesionCajaService.abrirSesion(request, usuarioId, LocalDateTime.now(clock));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Close an open session.
     */
    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasAuthority('caja:editar')")
    public SesionCajaResponse cerrarSesion(
            @PathVariable Long id,
            @Valid @RequestBody SesionCajaCerrarRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        return sesionCajaService.cerrarSesion(id, request, usuarioId, LocalDateTime.now(clock));
    }

    /**
     * Get the current open session for the authenticated user.
     */
    @GetMapping("/actual")
    @PreAuthorize("hasAuthority('caja:ver')")
    public SesionCajaResponse getSessionActual(Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        return sesionCajaService.getSessionActual(usuarioId);
    }

    /**
     * Get session detail by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('caja:ver')")
    public SesionCajaResponse getById(@PathVariable Long id) {
        return sesionCajaService.findById(id);
    }

    /**
     * Extract usuario ID from authentication principal name.
     * In a real implementation, this would extract from JWT claims or a custom principal.
     * For now, we parse the principal name as a Long.
     */
    private Long extractUsuarioId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return 0L;
        }
        String name = auth.getName();
        try {
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
