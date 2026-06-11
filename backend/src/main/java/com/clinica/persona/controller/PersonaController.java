package com.clinica.persona.controller;

import com.clinica.persona.dto.PersonaRequest;
import com.clinica.persona.dto.PersonaResponse;
import com.clinica.persona.dto.PersonaSearchResponse;
import com.clinica.persona.service.PersonaService;
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
@RequestMapping("/api/v1/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    public List<PersonaSearchResponse> search(
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidoPaterno) {
        return personaService.search(numeroDocumento, nombres, apellidoPaterno);
    }

    @GetMapping("/{id}")
    public PersonaResponse findById(@PathVariable Long id) {
        return personaService.findById(id);
    }

    @PostMapping
    public ResponseEntity<PersonaResponse> create(@Valid @RequestBody PersonaRequest request) {
        var response = personaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public PersonaResponse update(@PathVariable Long id, @Valid @RequestBody PersonaRequest request) {
        return personaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public PersonaResponse softDelete(@PathVariable Long id) {
        return personaService.softDelete(id);
    }
}
