package com.clinica.caja.comprobante.controller;

import com.clinica.caja.comprobante.dto.ComprobanteEmitirRequest;
import com.clinica.caja.comprobante.dto.ComprobanteResponse;
import com.clinica.caja.comprobante.dto.NotaCreditoRequest;
import com.clinica.caja.comprobante.dto.ReprintResponse;
import com.clinica.caja.comprobante.service.ComprobanteService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Comprobante REST controller.
 * Handles electronic invoice issuance (Boleta/Factura),
 * Nota de Crédito, reprint, and query operations.
 */
@RestController
@RequestMapping("/api/v1/caja/comprobante")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    public ComprobanteController(ComprobanteService comprobanteService) {
        this.comprobanteService = comprobanteService;
    }

    /**
     * Issue an electronic comprobante (Boleta or Factura) after payment.
     *
     * @param liquidacionId the payment liquidacion that triggers this invoice
     * @param request       issuance details
     * @param auth          security context
     * @return 201 with comprobante data including generated XML
     */
    @PostMapping("/{liquidacionId}/emitir")
    @PreAuthorize("hasAuthority('caja:crear')")
    public ResponseEntity<ComprobanteResponse> emitir(
            @PathVariable Long liquidacionId,
            @Valid @RequestBody ComprobanteEmitirRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        var response = comprobanteService.emitir(liquidacionId, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Issue a Nota de Crédito (07) against an existing comprobante.
     *
     * @param id      the original comprobante ID
     * @param request nota crédito details
     * @param auth    security context
     * @return 201 with Nota Crédito comprobante data
     */
    @PostMapping("/{id}/nota-credito")
    @PreAuthorize("hasAuthority('caja:anular')")
    public ResponseEntity<ComprobanteResponse> notaCredito(
            @PathVariable Long id,
            @Valid @RequestBody NotaCreditoRequest request,
            Authentication auth) {
        Long usuarioId = extractUsuarioId(auth);
        var response = comprobanteService.notaCredito(id, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Reprint a comprobante with "COPIA" watermark.
     * The stored XML is reused — no new XML is generated.
     *
     * @param id      comprobante ID
     * @param auth    security context
     * @param request HTTP request for IP logging
     * @return ReprintResponse with watermarked XML
     */
    @GetMapping("/{id}/reimprimir")
    @PreAuthorize("hasAuthority('caja:ver')")
    public ReprintResponse reimprimir(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest request) {
        Long usuarioId = extractUsuarioId(auth);
        String ipOrigen = request.getRemoteAddr();
        return comprobanteService.reimprimir(id, usuarioId, ipOrigen);
    }

    /**
     * Get a comprobante by ID.
     *
     * @param id         comprobante ID
     * @param includeXml whether to include the full XML (default: false)
     * @return ComprobanteResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('caja:ver')")
    public ComprobanteResponse findById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeXml) {
        return comprobanteService.findById(id, includeXml);
    }

    /**
     * List all comprobantes (header info only, no XML content).
     *
     * @return list of ComprobanteResponse
     */
    @GetMapping
    @PreAuthorize("hasAuthority('caja:ver')")
    public List<ComprobanteResponse> findAll() {
        return comprobanteService.findAll();
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
