package com.clinica.caja.tipocambio.controller;

import com.clinica.caja.tipocambio.dto.TipoCambioRequest;
import com.clinica.caja.tipocambio.dto.TipoCambioResponse;
import com.clinica.caja.tipocambio.service.TipoCambioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/caja/tipo-cambio")
public class TipoCambioController {

    private final TipoCambioService tipoCambioService;

    public TipoCambioController(TipoCambioService tipoCambioService) {
        this.tipoCambioService = tipoCambioService;
    }

    /**
     * Create a new exchange rate record.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('caja:crear')")
    public ResponseEntity<TipoCambioResponse> create(@Valid @RequestBody TipoCambioRequest request) {
        var response = tipoCambioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all exchange rate records.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('caja:ver', 'caja:crear')")
    public List<TipoCambioResponse> list() {
        return tipoCambioService.list();
    }

    /**
     * Get the latest exchange rate for a currency pair.
     */
    @GetMapping("/ultimo")
    @PreAuthorize("hasAnyAuthority('caja:ver', 'caja:crear')")
    public TipoCambioResponse getLatest(
            @RequestParam(defaultValue = "USD") String monedaOrigen,
            @RequestParam(defaultValue = "PEN") String monedaDestino) {
        return tipoCambioService.getLatest(monedaOrigen, monedaDestino);
    }
}
