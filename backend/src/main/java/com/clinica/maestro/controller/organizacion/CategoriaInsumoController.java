package com.clinica.maestro.controller.organizacion;

import com.clinica.maestro.dto.organizacion.CategoriaInsumoRequest;
import com.clinica.maestro.dto.organizacion.CategoriaInsumoResponse;
import com.clinica.maestro.service.organizacion.CategoriaInsumoService;
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
@RequestMapping("/api/v1/maestro/categoria-insumo")
public class CategoriaInsumoController {

    private final CategoriaInsumoService service;

    public CategoriaInsumoController(CategoriaInsumoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaInsumoResponse> findAll(
            @RequestParam(required = false) Integer categoriaPadreId) {
        return service.findAll(categoriaPadreId);
    }

    @GetMapping("/{id}")
    public CategoriaInsumoResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaInsumoResponse> create(
            @Valid @RequestBody CategoriaInsumoRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public CategoriaInsumoResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaInsumoRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public CategoriaInsumoResponse softDelete(@PathVariable Integer id) {
        return service.softDelete(id);
    }
}
