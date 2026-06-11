package com.clinica.maestro.controller;

import com.clinica.maestro.entity.rrhh.ConceptoPlanilla;
import com.clinica.maestro.repository.rrhh.ConceptoPlanillaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conceptos-planilla")
public class ConceptoPlanillaController {

    private final ConceptoPlanillaRepository repository;

    public ConceptoPlanillaController(ConceptoPlanillaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ConceptoPlanilla> findAll() {
        return repository.findAllByActivoTrueOrderByOrden();
    }
}
