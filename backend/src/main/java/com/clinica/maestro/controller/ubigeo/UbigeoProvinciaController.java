package com.clinica.maestro.controller.ubigeo;

import com.clinica.maestro.dto.ubigeo.UbigeoProvinciaRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoProvinciaResponse;
import com.clinica.maestro.service.ubigeo.UbigeoProvinciaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maestro/ubigeo/provincias")
public class UbigeoProvinciaController {

    private final UbigeoProvinciaService service;

    public UbigeoProvinciaController(UbigeoProvinciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<UbigeoProvinciaResponse> findAll(
            @RequestParam(name = "departamento", required = false) String departamentoCodigo) {
        if (departamentoCodigo != null) {
            return service.findByDepartamento(departamentoCodigo);
        }
        return service.findAll();
    }

    @GetMapping("/{codigo}")
    public UbigeoProvinciaResponse findById(@PathVariable String codigo) {
        return service.findById(codigo);
    }

    @PostMapping
    public ResponseEntity<UbigeoProvinciaResponse> create(
            @Valid @RequestBody UbigeoProvinciaRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{codigo}")
    public UbigeoProvinciaResponse update(
            @PathVariable String codigo,
            @Valid @RequestBody UbigeoProvinciaRequest request) {
        return service.update(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    public UbigeoProvinciaResponse softDelete(@PathVariable String codigo) {
        return service.softDelete(codigo);
    }
}
