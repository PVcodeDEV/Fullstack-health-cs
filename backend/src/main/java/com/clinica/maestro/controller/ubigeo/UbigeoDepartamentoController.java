package com.clinica.maestro.controller.ubigeo;

import com.clinica.maestro.dto.ubigeo.UbigeoDepartamentoRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoDepartamentoResponse;
import com.clinica.maestro.service.ubigeo.UbigeoDepartamentoService;
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
@RequestMapping("/api/v1/maestro/ubigeo/departamentos")
public class UbigeoDepartamentoController {

    private final UbigeoDepartamentoService service;

    public UbigeoDepartamentoController(UbigeoDepartamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<UbigeoDepartamentoResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{codigo}")
    public UbigeoDepartamentoResponse findById(@PathVariable String codigo) {
        return service.findById(codigo);
    }

    @PostMapping
    public ResponseEntity<UbigeoDepartamentoResponse> create(
            @Valid @RequestBody UbigeoDepartamentoRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{codigo}")
    public UbigeoDepartamentoResponse update(
            @PathVariable String codigo,
            @Valid @RequestBody UbigeoDepartamentoRequest request) {
        return service.update(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    public UbigeoDepartamentoResponse softDelete(@PathVariable String codigo) {
        return service.softDelete(codigo);
    }
}
