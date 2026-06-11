package com.clinica.maestro.controller.farmacia;

import com.clinica.maestro.dto.CatalogResponse;
import com.clinica.maestro.service.farmacia.MarcaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marcas")
public class MarcaController {

    private final MarcaService service;

    public MarcaController(MarcaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CatalogResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CatalogResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
