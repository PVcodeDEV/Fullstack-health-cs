package com.clinica.maestro.controller.financiero;

import com.clinica.maestro.dto.financiero.TipoComprobanteRequest;
import com.clinica.maestro.dto.financiero.TipoComprobanteResponse;
import com.clinica.maestro.service.financiero.TipoComprobanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maestro/tipos-comprobante")
public class TipoComprobanteController {

    private final TipoComprobanteService service;

    public TipoComprobanteController(TipoComprobanteService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoComprobanteResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TipoComprobanteResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TipoComprobanteResponse> create(
            @Valid @RequestBody TipoComprobanteRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public TipoComprobanteResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody TipoComprobanteRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Integer id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
