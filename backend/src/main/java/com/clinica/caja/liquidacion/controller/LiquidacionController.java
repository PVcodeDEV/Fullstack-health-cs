package com.clinica.caja.liquidacion.controller;

import com.clinica.caja.liquidacion.dto.LiquidacionResponse;
import com.clinica.caja.liquidacion.dto.PagoRequest;
import com.clinica.caja.liquidacion.dto.PreLiquidacionResponse;
import com.clinica.caja.liquidacion.service.LiquidacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Liquidación REST controller.
 * Handles pre-bill preview and payment processing.
 */
@RestController
@RequestMapping("/api/v1/caja/liquidacion")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;
    private final Clock clock;

    public LiquidacionController(LiquidacionService liquidacionService, Clock clock) {
        this.liquidacionService = liquidacionService;
        this.clock = clock;
    }

    /**
     * Generate a pre-liquidación (pre-bill) preview for patient review.
     * Accessible by CAJA, ADMIN, and MEDICO roles.
     */
    @GetMapping("/pre/{cuentaId}")
    @PreAuthorize("hasAuthority('caja:ver')")
    public PreLiquidacionResponse preLiquidar(@PathVariable Long cuentaId) {
        return liquidacionService.preLiquidar(cuentaId);
    }

    /**
     * Process payment for a Cuenta.
     * Only CAJA and ADMIN roles can process payments.
     */
    @PostMapping("/{cuentaId}/pagar")
    @PreAuthorize("hasAuthority('caja:crear')")
    public ResponseEntity<LiquidacionResponse> pagar(
            @PathVariable Long cuentaId,
            @Valid @RequestBody PagoRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        var response = liquidacionService.pagar(cuentaId, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Extract usuario ID from authentication principal name.
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
