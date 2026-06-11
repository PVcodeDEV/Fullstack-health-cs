package com.clinica.maestro.controller.organizacion;

import com.clinica.maestro.dto.organizacion.TipoDocumentoClinicoRequest;
import com.clinica.maestro.dto.organizacion.TipoDocumentoClinicoResponse;
import com.clinica.maestro.service.organizacion.TipoDocumentoClinicoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maestro/tipo-documento-clinico")
public class TipoDocumentoClinicoController {

    private final TipoDocumentoClinicoService service;

    public TipoDocumentoClinicoController(TipoDocumentoClinicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoDocumentoClinicoResponse> findAll(
            @RequestParam(required = false) Boolean requiereFirma) {
        return service.findAll(requiereFirma);
    }

    @GetMapping("/{id}")
    public TipoDocumentoClinicoResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TipoDocumentoClinicoResponse> create(
            @Valid @RequestBody TipoDocumentoClinicoRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public TipoDocumentoClinicoResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody TipoDocumentoClinicoRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public TipoDocumentoClinicoResponse softDelete(@PathVariable Integer id) {
        return service.softDelete(id);
    }
}
