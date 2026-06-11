package com.clinica.maestro.controller.financiero;

import com.clinica.maestro.dto.financiero.UnidadMedidaRequest;
import com.clinica.maestro.dto.financiero.UnidadMedidaResponse;
import com.clinica.maestro.service.financiero.UnidadMedidaService;
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
@RequestMapping("/api/v1/maestro/unidades-medida")
public class UnidadMedidaController {

    private final UnidadMedidaService service;

    public UnidadMedidaController(UnidadMedidaService service) {
        this.service = service;
    }

    @GetMapping
    public List<UnidadMedidaResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UnidadMedidaResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<UnidadMedidaResponse> create(
            @Valid @RequestBody UnidadMedidaRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public UnidadMedidaResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UnidadMedidaRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Integer id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
