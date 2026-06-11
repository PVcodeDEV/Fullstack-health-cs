package com.clinica.farmacia.almacen.controller;

import com.clinica.farmacia.almacen.dto.AlmacenRequest;
import com.clinica.farmacia.almacen.dto.AlmacenResponse;
import com.clinica.farmacia.almacen.service.AlmacenService;
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
@RequestMapping("/api/v1/farmacia/almacenes")
public class AlmacenController {

    private final AlmacenService service;

    public AlmacenController(AlmacenService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlmacenResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AlmacenResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('QUIMICO')")
    public ResponseEntity<AlmacenResponse> create(@Valid @RequestBody AlmacenRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('QUIMICO')")
    public AlmacenResponse update(@PathVariable Long id, @Valid @RequestBody AlmacenRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('QUIMICO')")
    public AlmacenResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
