package com.clinica.maestro.service.farmacia;

import com.clinica.maestro.dto.CatalogResponse;
import com.clinica.maestro.entity.farmacia.GrupoFarmacologico;
import com.clinica.maestro.repository.farmacia.GrupoFarmacologicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GrupoFarmacologicoService {

    private static final Logger log = LoggerFactory.getLogger(GrupoFarmacologicoService.class);

    private final GrupoFarmacologicoRepository repository;

    public GrupoFarmacologicoService(GrupoFarmacologicoRepository repository) {
        this.repository = repository;
    }

    public List<CatalogResponse> findAll() {
        return repository.findAllByActivoTrueOrderByCodigo()
            .stream()
            .map(e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getNombre()))
            .toList();
    }

    public CatalogResponse findById(Long id) {
        return repository.findById(id)
            .map(e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getNombre()))
            .orElseThrow(() -> new EntityNotFoundException("GrupoFarmacologico no encontrado con id: " + id));
    }
}
