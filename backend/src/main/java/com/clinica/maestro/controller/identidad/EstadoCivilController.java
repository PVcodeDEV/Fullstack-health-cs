package com.clinica.maestro.controller.identidad;

import com.clinica.maestro.dto.identidad.EstadoCivilRequest;
import com.clinica.maestro.dto.identidad.EstadoCivilResponse;
import com.clinica.maestro.service.identidad.EstadoCivilService;
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
@RequestMapping("/api/v1/maestro/estado-civil")
public class EstadoCivilController {

    private final EstadoCivilService service;

    public EstadoCivilController(EstadoCivilService service) {
        this.service = service;
    }

    @GetMapping
    public List<EstadoCivilResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EstadoCivilResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<EstadoCivilResponse> create(
            @Valid @RequestBody EstadoCivilRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public EstadoCivilResponse update(
            @PathVariable Long id,
            @Valid @RequestBody EstadoCivilRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public EstadoCivilResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
