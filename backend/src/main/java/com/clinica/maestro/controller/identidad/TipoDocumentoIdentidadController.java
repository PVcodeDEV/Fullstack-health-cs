package com.clinica.maestro.controller.identidad;

import com.clinica.maestro.dto.identidad.TipoDocumentoIdentidadRequest;
import com.clinica.maestro.dto.identidad.TipoDocumentoIdentidadResponse;
import com.clinica.maestro.service.identidad.TipoDocumentoIdentidadService;
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
@RequestMapping("/api/v1/maestro/tipo-documento-identidad")
public class TipoDocumentoIdentidadController {

    private final TipoDocumentoIdentidadService service;

    public TipoDocumentoIdentidadController(TipoDocumentoIdentidadService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoDocumentoIdentidadResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TipoDocumentoIdentidadResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TipoDocumentoIdentidadResponse> create(
            @Valid @RequestBody TipoDocumentoIdentidadRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public TipoDocumentoIdentidadResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TipoDocumentoIdentidadRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public TipoDocumentoIdentidadResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
