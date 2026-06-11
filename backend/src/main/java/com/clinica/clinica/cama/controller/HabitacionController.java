package com.clinica.clinica.cama.controller;

import com.clinica.clinica.cama.dto.HabitacionRequest;
import com.clinica.clinica.cama.dto.HabitacionResponse;
import com.clinica.clinica.cama.service.HabitacionService;
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
@RequestMapping("/api/v1/habitaciones")
@PreAuthorize("hasAuthority('cama:ver')")
public class HabitacionController {

    private final HabitacionService service;

    public HabitacionController(HabitacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<HabitacionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public HabitacionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cama:editar')")
    public ResponseEntity<HabitacionResponse> create(@Valid @RequestBody HabitacionRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cama:editar')")
    public HabitacionResponse update(@PathVariable Long id, @Valid @RequestBody HabitacionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cama:editar')")
    public HabitacionResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
