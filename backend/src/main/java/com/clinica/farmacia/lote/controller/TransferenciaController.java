package com.clinica.farmacia.lote.controller;

import com.clinica.farmacia.lote.dto.LoteResponse;
import com.clinica.farmacia.lote.dto.TransferenciaRequest;
import com.clinica.farmacia.lote.service.TransferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/farmacia/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('QUIMICO')")
    public ResponseEntity<LoteResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        var response = transferenciaService.transferir(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
