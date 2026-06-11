package com.clinica.maestro.controller.clinico;

import com.clinica.maestro.dto.clinico.EspecialidadMedicaRequest;
import com.clinica.maestro.dto.clinico.EspecialidadMedicaResponse;
import com.clinica.maestro.service.clinico.EspecialidadMedicaService;
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
@RequestMapping("/api/v1/maestro/especialidad-medica")
public class EspecialidadMedicaController {

    private final EspecialidadMedicaService service;

    public EspecialidadMedicaController(EspecialidadMedicaService service) {
        this.service = service;
    }

    @GetMapping
    public List<EspecialidadMedicaResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EspecialidadMedicaResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<EspecialidadMedicaResponse> create(
            @Valid @RequestBody EspecialidadMedicaRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public EspecialidadMedicaResponse update(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadMedicaRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public EspecialidadMedicaResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
