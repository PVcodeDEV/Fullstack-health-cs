package com.clinica.farmacia.reposicion.controller;

import com.clinica.farmacia.reposicion.dto.ReposicionGenerarRequest;
import com.clinica.farmacia.reposicion.dto.ReposicionResponse;
import com.clinica.farmacia.reposicion.service.ReposicionService;
import com.clinica.farmacia.reposicion.type.EstadoReposicion;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

import java.util.Map;

/**
 * REST controller for replenishment planning (lista de reposición).
 * Implements REP-01, SC-18, SC-19.
 */
@RestController
@RequestMapping("/api/v1/farmacia/reposicion")
public class ReposicionController {

    private final ReposicionService service;

    public ReposicionController(ReposicionService service) {
        this.service = service;
    }

    /**
     * Generate a replenishment list for products below stock threshold.
     * Only QUIMICO can generate new replenishment lists.
     */
    @PostMapping
    @PreAuthorize("hasRole('QUIMICO')")
    public ResponseEntity<ReposicionResponse> generar(
            @Valid @RequestBody ReposicionGenerarRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        var response = service.generar(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a reposicion by ID.
     * QUIMICO and ENCARGADO_SISTEMA can view.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('QUIMICO', 'ENCARGADO_SISTEMA')")
    public ReposicionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * List reposiciones by estado with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('QUIMICO', 'ENCARGADO_SISTEMA')")
    public Page<ReposicionResponse> list(
            @RequestParam(required = false, defaultValue = "PENDIENTE") String estado,
            Pageable pageable) {
        return service.listar(EstadoReposicion.valueOf(estado), pageable);
    }

    /**
     * Mark a reposicion as processed.
     */
    @PostMapping("/{id}/procesar")
    @PreAuthorize("hasRole('QUIMICO')")
    public ReposicionResponse marcarProcesada(@PathVariable Long id) {
        return service.marcarProcesada(id);
    }

    /**
     * Discard a reposicion with a reason.
     */
    @PostMapping("/{id}/descartar")
    @PreAuthorize("hasRole('QUIMICO')")
    public ReposicionResponse descartar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return service.descartar(id, motivo);
    }

    /**
     * Extract user ID from the Authentication principal.
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
