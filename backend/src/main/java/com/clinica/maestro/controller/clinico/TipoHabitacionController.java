package com.clinica.maestro.controller.clinico;

import com.clinica.maestro.dto.clinico.TipoHabitacionRequest;
import com.clinica.maestro.dto.clinico.TipoHabitacionResponse;
import com.clinica.maestro.service.clinico.TipoHabitacionService;
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
@RequestMapping("/api/v1/maestro/tipo-habitacion")
public class TipoHabitacionController {

    private final TipoHabitacionService service;

    public TipoHabitacionController(TipoHabitacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoHabitacionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TipoHabitacionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TipoHabitacionResponse> create(
            @Valid @RequestBody TipoHabitacionRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public TipoHabitacionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TipoHabitacionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public TipoHabitacionResponse softDelete(@PathVariable Long id) {
        return service.softDelete(id);
    }
}
