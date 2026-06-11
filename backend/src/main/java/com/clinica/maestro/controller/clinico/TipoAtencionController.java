package com.clinica.maestro.controller.clinico;

import com.clinica.maestro.dto.clinico.TipoAtencionRequest;
import com.clinica.maestro.dto.clinico.TipoAtencionResponse;
import com.clinica.maestro.service.clinico.TipoAtencionService;
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
@RequestMapping("/api/v1/maestro/tipo-atencion")
public class TipoAtencionController {

    private final TipoAtencionService service;

    public TipoAtencionController(TipoAtencionService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoAtencionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TipoAtencionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TipoAtencionResponse> create(
            @Valid @RequestBody TipoAtencionRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public TipoAtencionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TipoAtencionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public TipoAtencionResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
