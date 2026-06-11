package com.clinica.maestro.controller.clinico;

import com.clinica.maestro.dto.clinico.ViaAdministracionRequest;
import com.clinica.maestro.dto.clinico.ViaAdministracionResponse;
import com.clinica.maestro.service.clinico.ViaAdministracionService;
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
@RequestMapping("/api/v1/maestro/via-administracion")
public class ViaAdministracionController {

    private final ViaAdministracionService service;

    public ViaAdministracionController(ViaAdministracionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ViaAdministracionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ViaAdministracionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ViaAdministracionResponse> create(
            @Valid @RequestBody ViaAdministracionRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ViaAdministracionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ViaAdministracionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ViaAdministracionResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
