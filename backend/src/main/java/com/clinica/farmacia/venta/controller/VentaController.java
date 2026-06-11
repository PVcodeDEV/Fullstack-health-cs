package com.clinica.farmacia.venta.controller;

import com.clinica.farmacia.venta.dto.VentaRequest;
import com.clinica.farmacia.venta.dto.VentaResponse;
import com.clinica.farmacia.venta.service.VentaService;
import com.clinica.seguridad.service.UsuarioPrincipal;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farmacia/ventas")
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Complete a POS sale. vendedorUsuarioId extracted from security context.
     * Returns 201 with VentaResponse, or 400 (validation), 404 (not found),
     * 409 (stock conflict), 422 (business rule violation).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('QUIMICO', 'TECNICO', 'ENCARGADO_SISTEMA')")
    public ResponseEntity<VentaResponse> completar(@Valid @RequestBody VentaRequest request,
                                                   Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        try {
            var response = ventaService.completar(request, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (OptimisticLockException e) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, e.getMessage());
            problem.setTitle("Conflicto de stock");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    /**
     * Get a single sale with full detalles.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('QUIMICO', 'TECNICO', 'ENCARGADO_SISTEMA')")
    public VentaResponse findById(@PathVariable Long id) {
        return ventaService.findById(id);
    }

    /**
     * List all sales for a cash session (used by cash closing).
     */
    @GetMapping("/por-sesion/{sesionId}")
    @PreAuthorize("hasAnyRole('QUIMICO', 'TECNICO', 'ENCARGADO_SISTEMA')")
    public List<VentaResponse> findBySesionCaja(@PathVariable Long sesionId) {
        return ventaService.findBySesionCajaId(sesionId);
    }

    /**
     * List sales for a client (sales history).
     */
    @GetMapping("/por-cliente/{personaId}")
    @PreAuthorize("hasAnyRole('QUIMICO', 'TECNICO', 'ENCARGADO_SISTEMA')")
    public List<VentaResponse> findByCliente(@PathVariable Long personaId) {
        return ventaService.findByClientePersonaId(personaId);
    }

    /**
     * Soft-cancel a sale: restores stock, logs DEVOLUCION movement.
     */
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('QUIMICO', 'TECNICO', 'ENCARGADO_SISTEMA')")
    public ResponseEntity<VentaResponse> anular(@PathVariable Long id, Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        var response = ventaService.anular(id, usuarioId);
        return ResponseEntity.ok(response);
    }

    /**
     * Extract the authenticated user's ID from the UsuarioPrincipal.
     */
    private Long extractUsuarioId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up.getUsuario().getId();
        }
        log.warn("No se pudo extraer usuarioId del contexto de seguridad");
        return null;
    }
}
