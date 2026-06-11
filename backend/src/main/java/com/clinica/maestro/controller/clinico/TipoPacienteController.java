package com.clinica.maestro.controller.clinico;

import com.clinica.maestro.dto.clinico.TipoPacienteRequest;
import com.clinica.maestro.dto.clinico.TipoPacienteResponse;
import com.clinica.maestro.service.clinico.TipoPacienteService;
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
@RequestMapping("/api/v1/maestro/tipo-paciente")
public class TipoPacienteController {

    private final TipoPacienteService service;

    public TipoPacienteController(TipoPacienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoPacienteResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TipoPacienteResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TipoPacienteResponse> create(
            @Valid @RequestBody TipoPacienteRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public TipoPacienteResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TipoPacienteRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public TipoPacienteResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
