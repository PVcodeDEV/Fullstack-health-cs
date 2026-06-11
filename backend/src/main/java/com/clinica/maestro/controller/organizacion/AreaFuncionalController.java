package com.clinica.maestro.controller.organizacion;

import com.clinica.maestro.dto.organizacion.AreaFuncionalRequest;
import com.clinica.maestro.dto.organizacion.AreaFuncionalResponse;
import com.clinica.maestro.service.organizacion.AreaFuncionalService;
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
@RequestMapping("/api/v1/maestro/area-funcional")
public class AreaFuncionalController {

    private final AreaFuncionalService service;

    public AreaFuncionalController(AreaFuncionalService service) {
        this.service = service;
    }

    @GetMapping
    public List<AreaFuncionalResponse> findAll(
            @RequestParam(required = false) Boolean esAreaFisica) {
        return service.findAll(esAreaFisica);
    }

    @GetMapping("/{id}")
    public AreaFuncionalResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<AreaFuncionalResponse> create(
            @Valid @RequestBody AreaFuncionalRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AreaFuncionalResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody AreaFuncionalRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public AreaFuncionalResponse softDelete(@PathVariable Integer id) {
        return service.softDelete(id);
    }
}
