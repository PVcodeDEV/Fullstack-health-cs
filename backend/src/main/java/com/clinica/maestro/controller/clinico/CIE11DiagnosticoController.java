package com.clinica.maestro.controller.clinico;

import com.clinica.maestro.dto.clinico.CIE11DiagnosticoRequest;
import com.clinica.maestro.dto.clinico.CIE11DiagnosticoResponse;
import com.clinica.maestro.service.clinico.CIE11DiagnosticoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maestro/cie11")
public class CIE11DiagnosticoController {

    private final CIE11DiagnosticoService service;

    public CIE11DiagnosticoController(CIE11DiagnosticoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CIE11DiagnosticoResponse> findAll(
            @RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return service.search(q);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CIE11DiagnosticoResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CIE11DiagnosticoResponse> create(
            @Valid @RequestBody CIE11DiagnosticoRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
