package com.clinica.maestro.controller.clinico;

import com.clinica.maestro.dto.clinico.FormaFarmaceuticaRequest;
import com.clinica.maestro.dto.clinico.FormaFarmaceuticaResponse;
import com.clinica.maestro.service.clinico.FormaFarmaceuticaService;
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
@RequestMapping("/api/v1/maestro/forma-farmaceutica")
public class FormaFarmaceuticaController {

    private final FormaFarmaceuticaService service;

    public FormaFarmaceuticaController(FormaFarmaceuticaService service) {
        this.service = service;
    }

    @GetMapping
    public List<FormaFarmaceuticaResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public FormaFarmaceuticaResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<FormaFarmaceuticaResponse> create(
            @Valid @RequestBody FormaFarmaceuticaRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public FormaFarmaceuticaResponse update(
            @PathVariable Long id,
            @Valid @RequestBody FormaFarmaceuticaRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public FormaFarmaceuticaResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
