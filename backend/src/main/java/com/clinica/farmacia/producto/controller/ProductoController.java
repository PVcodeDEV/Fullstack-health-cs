package com.clinica.farmacia.producto.controller;

import com.clinica.farmacia.producto.dto.ActualizarUtilidadRequest;
import com.clinica.farmacia.producto.dto.ProductoRequest;
import com.clinica.farmacia.producto.dto.ProductoResponse;
import com.clinica.farmacia.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/farmacia/productos")
@PreAuthorize("hasAnyRole('QUIMICO','TECNICO','ENCARGADO_SISTEMA')")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductoResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProductoResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('QUIMICO')")
    public ResponseEntity<ProductoResponse> create(@Valid @RequestBody ProductoRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/utilidad")
    @PreAuthorize("hasRole('QUIMICO')")
    public ResponseEntity<ProductoResponse> actualizarUtilidad(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUtilidadRequest request) {
        ProductoResponse response = service.actualizarUtilidad(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('QUIMICO')")
    public ProductoResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
