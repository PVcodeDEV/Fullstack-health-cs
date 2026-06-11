package com.clinica.maestro.controller.ubigeo;

import com.clinica.maestro.dto.ubigeo.UbigeoDistritoRequest;
import com.clinica.maestro.dto.ubigeo.UbigeoDistritoResponse;
import com.clinica.maestro.service.ubigeo.UbigeoDistritoService;
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
@RequestMapping("/api/v1/maestro/ubigeo/distritos")
public class UbigeoDistritoController {

    private final UbigeoDistritoService service;

    public UbigeoDistritoController(UbigeoDistritoService service) {
        this.service = service;
    }

    @GetMapping
    public List<UbigeoDistritoResponse> findAll(
            @RequestParam(name = "provincia", required = false) String provinciaCodigo) {
        if (provinciaCodigo != null) {
            return service.findByProvincia(provinciaCodigo);
        }
        return service.findAll();
    }

    @GetMapping("/{codigo}")
    public UbigeoDistritoResponse findById(@PathVariable String codigo) {
        return service.findById(codigo);
    }

    @PostMapping
    public ResponseEntity<UbigeoDistritoResponse> create(
            @Valid @RequestBody UbigeoDistritoRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{codigo}")
    public UbigeoDistritoResponse update(
            @PathVariable String codigo,
            @Valid @RequestBody UbigeoDistritoRequest request) {
        return service.update(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    public UbigeoDistritoResponse softDelete(@PathVariable String codigo) {
        return service.softDelete(codigo);
    }
}
